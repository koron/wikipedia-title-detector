package main

import (
	"encoding/xml"
	"fmt"
	"io"
	"log"
	"os"
)

func main() {
	err := run()
	if err != nil {
		log.Fatal(err)
	}
}

func run() error {
	d, err := load(os.Stdin)
	if err != nil {
		return err
	}
	var w io.Writer = os.Stdout
	for _, item := range d.Items {
		fmt.Fprintln(w, item.Title)
	}
	return nil
}

type RDF struct {
	Items []*Item `xml:"item"`
}

type Item struct {
	Title string `xml:"title"`
}

func load(r io.Reader) (*RDF, error) {
	var v *RDF
	err := xml.NewDecoder(r).Decode(&v)
	if err != nil {
		return nil, err
	}
	return v, nil
}
