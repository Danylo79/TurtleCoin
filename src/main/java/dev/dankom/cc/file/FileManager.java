package dev.dankom.cc.file;

import dev.dankom.file.json.JsonFile;
import dev.dankom.file.json.JsonObjectBuilder;
import dev.dankom.file.type.Directory;

import java.util.ArrayList;

public class FileManager {
    public final JsonFile database = new JsonFile(new Directory("./coin"), "database",
            new JsonObjectBuilder()
                    .addArray("blockchain", new ArrayList<>())
                    .addArray("wallets", new ArrayList<>())
                    .build()
    );
}
