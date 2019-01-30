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
