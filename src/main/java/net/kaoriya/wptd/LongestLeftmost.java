package net.kaoriya.wptd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.kaoriya.ugmatcha2.Match;
import net.kaoriya.ugmatcha2.MatchHandler;

public class LongestLeftmost<T> implements MatchHandler<T>,
       Comparator<Match<T>>
{

    final ArrayList<Match<T>> raw;

    public LongestLeftmost() {
        this.raw = new ArrayList<>();
    }

    @Override
    public boolean matched(int index, String pattern, T value) {
        this.raw.add(new Match<T>(index, pattern, value));
        return true;
    }

    public ArrayList<Match<T>> finish() {
        ArrayList<Match<T>> result = new ArrayList<>();
        int curr = 0;
        Collections.sort(raw, this);
        for (Match<T> item : raw) {
            if (curr > item.index) {
                continue;
            }
            curr = item.index + item.pattern.length();
            result.add(item);
        }
        return result;
    }

    @Override
    public int compare(Match<T> a, Match<T> b) {
        if (a.index < b.index) {
            return -1;
        } else if (a.index > b.index) {
            return 1;
        }
        int la = a.pattern.length();
        int lb = b.pattern.length();
        if (la > lb) {
            return -1;
        } else if (la < lb) {
            return 1;
        }
        return a.pattern.compareTo(b.pattern);
    }

}
