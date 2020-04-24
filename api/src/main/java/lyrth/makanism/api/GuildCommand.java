package lyrth.makanism.api;

public abstract class GuildCommand extends Command {

    @Override
    public boolean isGuildOnly() {
        return true;
    }
}
