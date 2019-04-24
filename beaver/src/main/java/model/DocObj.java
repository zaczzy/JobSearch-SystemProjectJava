package model;

import java.util.ArrayList;
import java.util.List;

public class DocObj {
    int id;
    List<String> title = new ArrayList<>();
    List<String> meta = new ArrayList<>();
    List<String> h1 = new ArrayList<>();
    List<String> h2 = new ArrayList<>();
    List<String> content = new ArrayList<>();

    public DocObj(int id) {
        this.id = id;
    }
}
