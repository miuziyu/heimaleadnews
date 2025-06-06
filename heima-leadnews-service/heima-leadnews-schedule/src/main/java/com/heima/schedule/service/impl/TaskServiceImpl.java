package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.data.Json;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;



@Service
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {
    /**
     * @param task
     * @return
     */
    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private CacheService cacheService;

    /**
     * 添加任务
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {
        //1.添加task到数据库中
        boolean issuccess  = addTaskToDB(task);

        //2.添加任务到redis中
        if(issuccess)
        {
            addTaskToCache(task);
        }
        //2.1如果任务时间小于当前时间则进入list

        //2.2如果任务时间大于预设时间则进入zset中

        return task.getTaskId();
    }

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
       boolean flag = false;
       Task task = updateDb(taskId,ScheduleConstants.CANCELLED);
       if(task != null)
       {
           //删除redis中的数据
           reMoveTaskFromCache(task);
           flag = true;
       }
        return flag;
    }

    /**
     * 消费任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;

       try {
           //redis中拉出任务
           String key = type+"_"+priority;
           String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
           if(StringUtils.isNotBlank(task_json))
           {
               //把pop出来的json转化成真正的task
               task = JSON.parseObject(task_json,Task.class);
               //把task上传到数据库
               updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
           }
       }catch (Exception e)
       {
           e.printStackTrace();
           log.error("poll task exception");
       }

        return task;
    }




    private void reMoveTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();


        if(task.getExecuteTime()<=System.currentTimeMillis())
        {

            cacheService.lRemove(ScheduleConstants.TOPIC + key,0, JSON.toJSONString(task));
        }else {
            //删除zset中的数据
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }
    }



    private Task updateDb(long taskId, int status) {
        Task task = null;
        try
        {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());

        }
        catch (Exception e) {
            log.error("取消任务失败，taskId:{}", taskId);
            e.printStackTrace();
        }
        return task;

    }


    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        //获取5分钟之后的值，毫秒
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        //如果任务时间小于当前时间则进入list
        if(task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key,
                    JSON.toJSONString(task));
        } else if(task.getExecuteTime() <= nextScheduleTime)
            //如果任务时间大于预设时间且小于等于预设时间则进入zset中
            cacheService.zAdd(ScheduleConstants.FUTURE + key,
                    JSON.toJSONString(task),task.getExecuteTime());

        }



    private boolean addTaskToDB(Task task) {
        boolean flag = false;
        try {

            //保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            //设置taskid
            task.setTaskId(taskinfo.getTaskId());
            //保存任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        }catch (Exception e) {
            e.printStackTrace();
        }


        return flag;


    }
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isBlank(token)) {
            System.out.println(System.currentTimeMillis() / 1000 + "执行了定时任务");
            //查出所有待完成集合
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                //拼接出topickey（如果要转换需要用到）
                String topickey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                //在待完成集合中遍历到时间的任务
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                //将这些任务放到消费者队列中
                cacheService.refreshWithPipeline(futureKey,topickey,tasks);
                System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topickey + "下");
            }
        
        }



    }

    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    public void reloadData(){
        //1.清除所有的缓存
        clearCache();
        log.info("数据库数据同步到缓存");
        //2.查询所有小于未来5分钟的任务
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        List<Taskinfo> alltask = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery()
                .lt(Taskinfo::getExecuteTime, calendar.getTime()));
        if(alltask != null && alltask.size() > 0)
        {
            for (Taskinfo taskinfo : alltask) {
                //3.将任务放入到redis中
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
    }

    private void clearCache() {
        //删除缓存中未来数据集合和当前霞飞这队列的所有key
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        cacheService.delete(futureKeys);
        cacheService.delete(topicKeys);

    }

}
