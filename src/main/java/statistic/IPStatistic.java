package statistic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-14 14:49
 */
public class IPStatistic {
    public static void main(String[] args) {
        String successPath = "/data/mongoinfo/result/ip_more_success.txt";
        Set<String> ipsuccessSet = new HashSet<>();
        String regex = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
        Pattern pattern = Pattern.compile(regex);
        try {
            Files.lines(Paths.get(successPath)).forEach(line -> {
                if (pattern.matcher(line).matches()) {
                    ipsuccessSet.add(line);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(ipsuccessSet.size());

        String errorPath = "/data/mongoinfo/result/ip_more_error.txt";
        Set<String> iperrorSet = new HashSet<>();
        try {
            Files.lines(Paths.get(errorPath)).forEach(line -> {
                if (pattern.matcher(line).matches()) {
                    iperrorSet.add(line);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(iperrorSet.size());

        Set<String> result = new HashSet<>();
        result.addAll(ipsuccessSet );
        result.addAll(iperrorSet);

        System.out.println(result.size());
    }
}
