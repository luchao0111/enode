package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class ChangeInheritTestAggregateTitleCommand extends Command {
    public String Title;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}