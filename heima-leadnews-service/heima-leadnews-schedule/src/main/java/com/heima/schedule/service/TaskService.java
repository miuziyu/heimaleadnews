package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;
import org.springframework.stereotype.Service;

@Service
public interface TaskService {
    public long addTask(Task task);
    public boolean cancelTask(long taskId);
    public Task poll(int type,int priority);
}
