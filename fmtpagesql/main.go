package main

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"log"
	"os"
	"strconv"
	"strings"
	"unicode/utf8"
)

func main() {
	err := formatPageSQL(os.Stdin, os.Stdout)
	if err != nil {
		log.Fatal(err)
	}
}

const insertPrefix = "INSERT INTO `page` VALUES "

func formatPageSQL(r io.Reader, w io.Writer) error {
	br := bufio.NewReaderSize(r, 1024*1024)
	bw := bufio.NewWriter(w)
	defer bw.Flush()
	ln := 0
	for {
		b, p, err := br.ReadLine()
		if err != nil {
			if err == io.EOF {
				break
			}
			return err
		}
		if p {
			return errors.New("a line is over 1MB")
		}
		ln += 1
		l := string(b)
		if !strings.HasPrefix(l, insertPrefix) {
			continue
		}
		l = l[len(insertPrefix):]
		for len(l) > 0 {
			vals, l2, err := parseValues(l)
			if err != nil {
				return err
			}
			l = l2
			if len(vals) > 0 {
				//fmt.Fprintf(bw, "%v\n", vals)
				for i, v := range vals {
					if i > 0 {
						fmt.Fprint(bw, "\t")
					}
					fmt.Fprintf(bw, "%#v", v)
				}
				fmt.Fprint(bw, "\n")
			}
		}
		log.Printf("line %d\n", ln)
	}
	return nil
}

func decodeRune(s string) (rune, string) {
	r, n := utf8.DecodeRuneInString(s)
	return r, s[n:]
}

func parseValues(s string) ([]interface{}, string, error) {
	vals := make([]interface{}, 0, 15)
	var (
		r   rune
		v   interface{}
		err error
	)
prelude:
	for {
		r, s = decodeRune(s)
		switch r {
		case ';':
			return nil, "", nil
		case '(':
			break prelude
		case ',':
			continue
		default:
			return nil, "", fmt.Errorf("unexpedted character: expected '(' or ',', but actually '%q'", r)
		}
	}
	for len(s) > 0 {
		v, s, err = parseOneValue(s)
		if err != nil {
			return nil, s, err
		}
		vals = append(vals, v)
		r, s = decodeRune(s)
		switch r {
		case ',':
			continue
		case ')':
			return vals, s, nil
		default:
			return nil, "", fmt.Errorf("unexpected character %x", r)
		}
	}
	return vals, s, nil
}

func parseOneValue(s string) (interface{}, string, error) {
	r, s2 := decodeRune(s)
	if r == '\'' {
		// parse a string.
		return parseOneString(s2)
	}
	n := strings.IndexAny(s, ",)")
	if n < 0 {
		return nil, "", fmt.Errorf("no terminator of a value: %q", s)
	}
	s, s2 = s[:n], s[n:]
	if s == "NULL" {
		return nil, s2, nil
	}
	if n := strings.Index(s, "."); n >= 0 {
		f, err := strconv.ParseFloat(s, 64)
		if err != nil {
			return nil, "", err
		}
		return f, s2, nil
	}
	n64, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		return nil, "", err
	}
	return n64, s2, nil
}

func parseOneString(s string) (string, string, error) {
	var (
		b = &strings.Builder{}
		r rune
		f bool
	)
	for len(s) > 0 {
		r, s = decodeRune(s)
		if f {
			b.WriteRune(r)
			f = false
			continue
		}
		switch r {
		case '\'':
			return b.String(), s, nil
		case '\\':
			f = true
			continue
		default:
			b.WriteRune(r)
		}
	}
	return b.String(), s, nil
}
