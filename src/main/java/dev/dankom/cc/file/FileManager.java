package dev.dankom.cc.file;

import dev.dankom.file.json.JsonFile;
import dev.dankom.file.json.JsonObjectBuilder;
import dev.dankom.file.type.Directory;

import java.util.ArrayList;

public class FileManager {
    public final JsonFile blockchain = new JsonFile(new Directory("./ccoin"), "blockchain", new JsonObjectBuilder().addArray("blocks", new ArrayList<>()).build());
}