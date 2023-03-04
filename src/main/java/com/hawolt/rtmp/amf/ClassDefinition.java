package com.hawolt.rtmp.amf;


import java.util.Arrays;

/**
 * Class Definition for AMF3
 *
 * @author Hawolt
 */
public class ClassDefinition {
    public boolean externalizable;
    public String[] properties;
    public String className;
    public byte encoding;

    @Override
    public String toString() {
        return "ClassDefinition{" +
                "externalizable=" + externalizable +
                ", properties=" + Arrays.toString(properties) +
                ", className='" + className + '\'' +
                ", encoding=" + encoding +
                '}';
    }
}
