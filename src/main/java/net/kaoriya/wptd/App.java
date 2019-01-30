/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package net.kaoriya.wptd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import net.kaoriya.ugmatcha2.AhoCorasick;
import net.kaoriya.ugmatcha2.MatchHandler;

public class App {

    static final String DICT = "build/dict/jawiki-20190120-all-titles-in-ns0";

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
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                aho.add(line, Boolean.TRUE);
            }
        }
        aho.compile();
        return aho;
    }

    public void find(File f) throws Exception {
        System.out.printf("in file %s:\n", f.getName());
        byte[] b = Files.readAllBytes(f.toPath());
        String text= new String(b, "UTF-8");
        find(text);
    }

    public void find(String text) throws Exception {
        AhoCorasick<Boolean> aho = loadDict();
        aho.match(text, new MatchHandler<Boolean>() {
            @Override
            public boolean matched(int index, String pattern, Boolean value) {
                //System.out.printf("- found \"%s\" at index %d\n", pattern, index);
                return true;
            }
        }, 0);
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        File d = new File(TEXTDIR);
        for (File f : d.listFiles()) {
            app.find(f);
        }
    }
}
