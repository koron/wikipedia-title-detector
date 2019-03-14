package net.kaoriya.wptd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;

import net.kaoriya.ugmatcha2.AhoCorasick;
import net.kaoriya.ugmatcha2.Match;
import net.kaoriya.ugmatcha2.MatchHandler;

public class App {

    static final String DICT = "build/dict/jawiki-20190120-all-titles-in-ns0";
    //static final String DICT = "build/dict/dict0";

    static final String TEXTDIR = "build/text";

    static double MEGABYTES = 1024 * 1024;

    AhoCorasick<Boolean> cache;

    AhoCorasick<Boolean> loadDict() throws IOException {
        if (this.cache == null) {
            System.err.println("# loading dict");
            Runtime rt = Runtime.getRuntime();
            System.gc();
            System.gc();

            long start = System.nanoTime();
            this.cache = loadDict(new File(DICT));
            double seconds = (System.nanoTime() - start) / 1e9;

            System.gc();
            System.gc();
            long mem = rt.totalMemory() - rt.freeMemory();
            System.err.printf(
                    "# loaded dict in %6.3fsec, memory used %.3fMB\n",
                    seconds, mem / MEGABYTES);
        }
        return this.cache;
    }

    AhoCorasick<Boolean> loadDict(File f) throws IOException {
        AhoCorasick<Boolean> aho = new AhoCorasick<>();
        System.err.printf("# from file: %s\n", f.getName());
        long count = 0;
        long start = System.nanoTime();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.length() < 2) {
                    continue;
                }
                aho.add(line, Boolean.TRUE);
                count++;
            }
        }
        System.err.printf("# loaded %d words in %.3fsec\n", count, (System.nanoTime() - start) / 1e9);
        start = System.nanoTime();
        aho.compile();
        System.err.printf("# compiled in %.3fsec\n", (System.nanoTime() - start) / 1e9);
        return aho;
    }

    public void find(File f) throws Exception {
        System.out.printf("in file %s:\n", f.getName());
        System.err.printf("# finding in file %s\n", f.getName());
        byte[] b = Files.readAllBytes(f.toPath());
        String text= new String(b, "UTF-8");
        Stat stat = find(text);
        System.err.printf("# found %d words in %.3fsec, memory used: %.3fMB\n", stat.count, stat.elapsedNano / 1e9, stat.memBytes / MEGABYTES);
    }

    public static class Stat {
        public long count;
        public long elapsedNano;
        public long memBytes;
    }

    public Stat find(String text) throws Exception {
        AhoCorasick<Boolean> aho = loadDict();
        final Stat stat = new Stat();
        Runtime rt = Runtime.getRuntime();
        System.gc();
        System.gc();
        long start = System.nanoTime();
        aho.match(text, new MatchHandler<Boolean>() {
            @Override
            public boolean matched(int index, String pattern, Boolean value) {
                System.out.printf("- found \"%s\" at index %d\n", pattern, index);
                stat.count++;
                return true;
            }
        }, 0);
        stat.elapsedNano = System.nanoTime() - start;
        System.gc();
        System.gc();
        stat.memBytes =  rt.totalMemory() - rt.freeMemory();
        return stat;
    }

    // benchmark loading the dict, finding words from dictionary.
    void app0() throws Exception {
        File d = new File(TEXTDIR);
        for (File f : d.listFiles()) {
            find(f);
        }
    }

    void app1(File f, PrintStream out) throws Exception {
        AhoCorasick<Boolean> aho = loadDict();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) {
                LongestLeftmost<Boolean> ll = new LongestLeftmost<>();
                aho.match(line, ll, 0);
                ArrayList<Match<Boolean>> result = ll.finish();
                out.printf("%s\n", line);
                for (Match item : result) {
                    out.printf("\t%s", item.pattern);
                }
                out.print("\n\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        //app.app0();
        //app.app1(new File("testdata/hotentry-20190306T1059JST.txt"), System.out);
        app.app1(new File("tmp/mid.txt"), System.out);
    }
}
