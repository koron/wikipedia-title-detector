## Preparation

1. Download <https://dumps.wikimedia.org/jawiki/20190120/jawiki-20190120-all-titles-in-ns0.gz>.
2. Extract it as `jawiki-20190120-all-titles-in-ns0`.
3. Put it in `build/dict/` directory.
4. Put target files in `build/text` directory.

## How to run

```
$ ./gradlew run
```

## Output example

```
in file 2011-08-13-2.md:
# loading dict
# loaded dict in  8.759sec, memory used 790.290MB
```

## Mecab

```console
$ time mecab < build/dict/jawiki-20190120-all-titles-in-ns0 > build/out.dict

real    0m20.522s
user    0m0.000s
sys     0m0.046s

$ wc -l build/out.dict
7679069 build/out.dict

$ grep -nr ^EOS build/out.dict | wc -l
1831242

$ wc -l build/dict/jawiki-20190120-all-titles-in-ns0
1831241 build/dict/jawiki-20190120-all-titles-in-ns0
```

## Links

*   How to use dictionaries
    *   [Layout of Database](https://www.mediawiki.org/wiki/Manual:Database_layout/ja)
    *   <http://www.mwsoft.jp/programming/munou/wikipedia_data_list.html>
*   Wikipedia data
    *   Japanese <https://dumps.wikimedia.org/jawiki/>
    *   English: <https://dumps.wikimedia.org/enwiki/>
