package algalon.client;
import algalon.logon.Version;
import algalon.logon.client.Session;
import java.net.InetSocketAddress;
import static algalon.utils.StringsKt.reverse_array;
import static algalon.utils.StringsKt.uppercase_ascii;
import java.util.function.Consumer;

public final class Client
{
    // ---------------------------------------------------------------------------------------------

    public String username;
    public String password;
    public Version version;
    public InetSocketAddress server;
    public byte[] ip;
    public byte[] game;
    public byte[] platform;
    public byte[] os;
    public byte[] lang;
    public int timezone_bias  = 0;
    public long read_timeout  = 10_000;
    public long write_timeout = 10_000;

    // ---------------------------------------------------------------------------------------------

    public Client(Consumer<Client> init)
    {
        // default values
        game     ("WoW");
        platform ("x86");
        os       ("Win");
        lang     ("enUS");

        init.accept(this);
    }

    // ---------------------------------------------------------------------------------------------

    public Client username (String username)
    {
        this.username = uppercase_ascii(username);
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public Client password (String password)
    {
        this.password = uppercase_ascii(password);
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public Client game (String game)
    {
        this.game = reverse_array(game, true);
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public Client platform (String platform)
    {
        this.platform = reverse_array(platform, true);
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public Client os (String os)
    {
        this.os = reverse_array(os, true);
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public Client lang (String lang)
    {
        this.lang = reverse_array(lang, false);
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public void start()
    {
        new Session(this).connect(server);
    }

    // ---------------------------------------------------------------------------------------------

    public void start_trace (boolean trace)
    {
        Session session = new Session(this);
        session.trace = trace;
        session.connect(server);
    }

    // ---------------------------------------------------------------------------------------------
}
