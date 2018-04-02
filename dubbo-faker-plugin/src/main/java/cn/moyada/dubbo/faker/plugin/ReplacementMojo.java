package cn.moyada.dubbo.faker.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;

/**
 * @author xueyikang
 * @create 2018-04-01 18:49
 */
@Mojo(name = "tag")
public class ReplacementMojo extends AbstractMojo {

    @Parameter(property = "source", defaultValue = "")
    private String source;

    @Parameter(property = "tag", defaultValue = "")
    private String tag;

    @Parameter(property = "target", defaultValue = "")
    private String target;

    @Parameter(property = "replacement", defaultValue = "")
    private String replacement;

    @Override
    public void execute() {
        if(source.trim().equals("")) {
            System.out.println("source can not be null.");
            return;
        }
        if(tag.trim().equals("")) {
            System.out.println("tag can not be null.");
            return;
        }
        if(target.trim().equals("")) {
            System.out.println("target can not be null.");
            return;
        }
        if(replacement.trim().equals("")) {
            System.out.println("replace can not be null.");
            return;
        }

        FileFetch fileFetch;
        FileReplace fileReplace;
        try {
            fileFetch = new FileFetch(source);
            fileReplace = new FileReplace(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String contain = fileFetch.fetch("<" + tag + ">", "</" + tag + ">");
        if(null == contain) {
            System.out.println("can not fetch <" + tag + "></" + tag + "> tag in file: " + source);
            return;
        }

        System.out.println("find <" + tag + "> tag in file: " + source);

        fileReplace.replace(replacement, contain);

        System.out.println("replace tag: <!--@" + replacement + "-->  success in file: " + tag);
    }
}