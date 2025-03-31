package com.tyron.builder.ide.common.blame;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.android.ide.common.blame.SourceFile;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import java.io.IOException;

public class SourceFilePositionJsonSerializer extends TypeAdapter<SourceFilePosition> {

    private static final String POSITION = "position";

    private static final String FILE = "file";

    private final SourceFileJsonTypeAdapter mSourceFileJsonTypeAdapter;
    private final SourcePositionJsonTypeAdapter mSourcePositionJsonTypeAdapter;

    public SourceFilePositionJsonSerializer() {
        mSourcePositionJsonTypeAdapter = new SourcePositionJsonTypeAdapter();
        mSourceFileJsonTypeAdapter = new SourceFileJsonTypeAdapter();
    }

    @Override
    public SourceFilePosition read(JsonReader in) throws IOException {
        in.beginObject();
        SourceFile file = SourceFile.UNKNOWN;
        SourcePosition position = SourcePosition.UNKNOWN;
        while(in.hasNext()) {
            String name = in.nextName();
            if (name.equals(FILE)) {
                file = mSourceFileJsonTypeAdapter.read(in);
            } else if (name.equals(POSITION)) {
                position = mSourcePositionJsonTypeAdapter.read(in);
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        return new SourceFilePosition(file, position);
    }

    @Override
    public void write(JsonWriter out, SourceFilePosition src) throws IOException {
        out.beginObject();
        SourceFile sourceFile = src.getFile();
        if (!sourceFile.equals(SourceFile.UNKNOWN)) {
            out.name(FILE);
            mSourceFileJsonTypeAdapter.write(out, sourceFile);
        }
        SourcePosition position = src.getPosition();
        if (!position.equals(SourcePosition.UNKNOWN)) {
            out.name(POSITION);
            mSourcePositionJsonTypeAdapter.write(out, position);
        }
        out.endObject();
    }

    /* package */ SourcePositionJsonTypeAdapter getSourcePositionTypeAdapter() {
        return mSourcePositionJsonTypeAdapter;
    }
}