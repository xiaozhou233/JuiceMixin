package cn.xiaozhou233.JuiceMixin;


import java.lang.instrument.Instrumentation;

public class JuiceMixin {
    private static Instrumentation inst = null;
    public JuiceMixin(Instrumentation inst){
        this.inst = inst;
    }

    public void run(){
        Transformer.redefineClass();
    }

    public static Instrumentation getInst() {
        return inst;
    }
}