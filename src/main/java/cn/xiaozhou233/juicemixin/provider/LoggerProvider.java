package cn.xiaozhou233.juicemixin.provider;

public interface LoggerProvider {
    void info(String str, Object ...o);
    void warn(String str, Object ...o);
    void error(String str, Object ...o);
    void debug(String str, Object ...o);
}
