package main

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"log"
	"os"
	"strings"
)

func main() {
	err := formatMecab(os.Stdin, os.Stdout)
	if err != nil {
		log.Fatal(err)
	}
}

type data struct {
	text  string
	attrs []string

	attr0 string
	attr1 string
	attr2 string
}

func parseData(s string) (*data, error) {
	n := strings.Index(s, "\t")
	if n < 0 {
		return nil, errors.New("can't find TAB")
	}
	text, remain := s[:n], s[n+1:]
	attrs := strings.Split(remain, ",")
	var attr0, attr1, attr2 string
	m := len(attrs)
	if m >= 0 {
		attr0 = attrs[0]
	}
	if m >= 1 {
		attr1 = attrs[1]
	}
	if m >= 2 {
		attr2 = attrs[2]
	}
	return &data{
		text:  text,
		attrs: attrs,
		attr0: attr0,
		attr1: attr1,
		attr2: attr2,
	}, nil
}

func formatMecab(r io.Reader, w io.Writer) error {
	br := bufio.NewReader(r)
	sentense := make([]*data, 0, 30)
	for {
		l, _, err := br.ReadLine()
		if err != nil {
			if err == io.EOF {
				return nil
			}
			return err
		}
		s := string(l)
		if s == "EOS" {
			fmt.Fprintf(w, "%s\n", joinTitle(sentense))
			for _, s := range words(sentense) {
				fmt.Fprintf(w, "\t%s", s)
			}
			fmt.Fprint(w, "\n\n")
			sentense = sentense[:0]
			continue
		}
		d, err := parseData(s)
		if err != nil {
			return err
		}
		sentense = append(sentense, d)
	}
}

func joinTitle(sentense []*data) string {
	b := &strings.Builder{}
	for _, d := range sentense {
		b.WriteString(d.text)
	}
	return b.String()
}

func words(sentense []*data) []string {
	w := make([]string, 0, len(sentense))
	for _, d := range sentense {
		if d.attr0 == "名詞" {
			w = append(w, d.text)
		}
	}
	return w
}
