package cn.xiaozhou233.juicemixin.test;

public class TargetClass {

    public void run() {
        System.out.println("Target");
    }

    public int add(int a, int b) {
        return Math.addExact(a, b);
    }

    public static String name() {
        return "target";
    }
}
