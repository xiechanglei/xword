package com.xiechanglei.code.wordx.segmentation;

import java.util.Objects;

/**
 * 词、拼音、词性、词频
 * Word
 */
public class Word implements Comparable<Object> {
    private String text;
    private String pos = null;
    private int frequency;

    public Word(String text) {
        this.text = text;
    }

    public Word(String text, String pos, int frequency) {
        this.text = text;
        this.pos = pos;
        this.frequency = frequency;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(this.text);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        return Objects.equals(this.text, other.text);
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (this.text == null) {
            return -1;
        }
        if (o == null) {
            return 1;
        }
        if (!(o instanceof Word)) {
            return 1;
        }
        String t = ((Word) o).getText();
        if (t == null) {
            return 1;
        }
        return this.text.compareTo(t);
    }
}
