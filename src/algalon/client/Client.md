# Client

- usernames must be uppercased (only the ASCII chars)
- passwords must be uppercased (only the ASCII chars)

Use the `username(String)` and `password(String)` setters to ensure this.

- `game`, `platform` and `os` hold strings converted to reversed and zero-terminated byte arrays.
- `lang` holds a string converted to a reversed byte array.

Use the `game(String)`, `platform(String)`, `os(String)` and `lang(String)` setters to ensure this.

- All WoW clients always send "WoW" as `game`.
- `platform`: "x86" or "PPC" (maybe "x64" as well in newer versions?)
- `os`: "Win" or "OSX"
- `timezone_bias`: local time - UTC in minutes.
- `read_timeout` and `write_timeout`: timeouts in milliseconds for client reads/writes.