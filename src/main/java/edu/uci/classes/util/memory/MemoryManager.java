package edu.uci.classes.util.memory;

import java.util.*;

public class MemoryManager
{

    // Return the list of values that need to be removed from a given Hashmap because their destruction time has passed.
    // @param mapEntrySet the entry set from a given HashMap.
    // @param memoryDestruction the code we need to execute in order to get the destruction time from the
    // value of a given index in the entry set.
    public static <T, V> List<T> getMemoryToRemove(Set<Map.Entry<T, V>> mapEntrySet, MemoryDestruction memoryDestruction)
    {
        ArrayList<T> memoryToRemove = new ArrayList<>();

        for(Map.Entry<T, V> entry : mapEntrySet)
        {
            if(memoryDestruction.getMemoryDestructionTime(entry.getValue()) > System.currentTimeMillis())
                continue;

            memoryToRemove.add(entry.getKey());
        }

        return memoryToRemove;
    }

}