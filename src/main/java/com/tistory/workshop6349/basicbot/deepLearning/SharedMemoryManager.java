package com.tistory.workshop6349.basicbot.deepLearning;

public class SharedMemoryManager {

    private static SharedMemoryManager sharedMemoryManager = new SharedMemoryManager();
    public static SharedMemoryManager getInstance() {
        return sharedMemoryManager;
    }

}
