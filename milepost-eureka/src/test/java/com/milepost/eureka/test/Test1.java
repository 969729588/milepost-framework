package com.milepost.eureka.test;

import org.junit.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Created by Ruifu Hua on 2020/1/14.
 */
public class Test1 {

    class SpringBootBanner implements Banner {

        private final String[] BANNER = { "",
                "  .   ____          _            __ _ _",
                " /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\",
                "( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\",
                " \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )",
                "  '  |____| .__|_| |_|_| |_\\__, | / / / /",
                " =========|_|==============|___/=/_/_/_/" };

        private static final String SPRING_BOOT = " :: Spring Boot :: ";

        private static final int STRAP_LINE_SIZE = 42;

        @Override
        public void printBanner(Environment environment, Class<?> sourceClass,
                                PrintStream printStream) {
            for (String line : BANNER) {
                printStream.println(line);
            }
            String version = SpringBootVersion.getVersion();
            version = (version != null) ? " (v" + version + ")" : "";
            StringBuilder padding = new StringBuilder();
            while (padding.length() < STRAP_LINE_SIZE
                    - (version.length() + SPRING_BOOT.length())) {
                padding.append(" ");
            }

            printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT,
                    AnsiColor.DEFAULT, padding.toString(), AnsiStyle.FAINT, version));
            printStream.println();
        }

    }

    @Test
    public void test1(){
        Banner DEFAULT_BANNER = new SpringBootBanner();
    }
}
