package net.sourceforge.cobertura.instrument;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassPatternTest {

    @Test
    public void testRegex() throws Exception {
        ClassPattern classPattern = new ClassPattern();
        String[] includeRegexes = {"org.apache.ranger.*"};
        String[] excludeRegexes = {
                ".*org.apache.ranger.authorization.hadoop.RangerHdfsAuthorizer.*",
                "net.sourceforge.cobertura.*",
                ".*[tT][eE][sS][tT].*",
                ".*Proto.*",
                ".*PBImpl.*",
                ".*api.*",
                ".*portmap.*",
                ".*oncrpc.*",
                ".*protobuf.*",
                ".*org.apache.ranger.authorization.hadoop.RangerHdfsAuthorizer.*",

        };
        String[] mustNotMatch = {
                "org.apache.ranger.authorization.hadoop.RangerHdfsAuthorizer",
                "org.apache.ranger.authorization.hadoop.RangerHdfsAuthorizer$RangerAccessControlEnforcer",
        };
        String[] mustMatch = {
                "org.apache.ranger.others",
                "org.apache.ranger.authorization.others",
        };
        for (String s : includeRegexes) {
            classPattern.addIncludeClassesRegex(s);
        }
        for (String s : excludeRegexes) {
            classPattern.addExcludeClassesRegex(s);
        }
        for (String s : mustNotMatch) {
            assertFalse(classPattern.matches(s));
        }
        for (String s : mustMatch) {
            assertTrue(classPattern.matches(s));
        }
    }

}
