package ru.jchat.core.examples;

public class ExampleOne {
    public ExampleOne(){}

    public int[] get(int[] in){
        int[] out = null;

        if(in == null || in.length == 0){
            throw new RuntimeException();
        }

        for(int i = in.length-1; i > 0; i--){
            if(in[i] == 4){
                int outLength = in.length - (i + 1);
                out = new int[outLength];
                for(int a = 0; a < outLength; a++){
                    out[a] = in[++i];
                }
                break;
            }
        }
        if(out == null){
            throw new RuntimeException();
        }
        return out;
    }
}
