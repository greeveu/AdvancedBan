package me.leoko.advancedban.utils.tabcompletion;

import java.util.ArrayList;
import java.util.Arrays;

public interface MutableTabCompleter extends TabCompleter {
    @Override
    ArrayList<String> onTabComplete(Object user, String[] args);

    @SafeVarargs
    static <T> ArrayList<T> list(T... elements){
        return new ArrayList<>(Arrays.asList(elements));
    }
}
