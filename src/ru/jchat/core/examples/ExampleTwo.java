package ru.jchat.core.examples;

public class ExampleTwo {

    public ExampleTwo(){}

    public boolean get(int[] in){
        boolean out = false;
        if(in != null && in.length > 0){
            boolean one = false;
            boolean four = false;
            boolean other = false;
            for(int el : in){
                switch (el) {
                    case 1:
                        one = true;
                        break;
                    case 4:
                        four = true;
                        break;
                    default:
                        other = true;
                        break;
                }
            }
            if(one && four && !other){
                out = true;
            }
        }
        return out;
    }
}
