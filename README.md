# Groundhog

Groundhog is a small supervisor for the [Hedgehog](https://github.com/unigrid-project/hedgehog) node of
the Unigrid network. It starts Hedgehog, restarts it when it dies, stops it when Groundhog exits, and keeps
the Hedgehog executable on the latest stable release.

## What it does

- **Starts** `hedgehog.bin daemon`, with `--netport=<port>` when a p2p port is given.
- **Watches** the process and restarts it if it has died. The check runs every two minutes.
- **Stops** Hedgehog with `hedgehog.bin cli stop` when Groundhog is shut down.
- **Updates** Hedgehog by itself, see below.

## Requirements

- Java 17 or later.
- Maven, to build.
- Linux x86_64 for automatic updates. Hedgehog publishes no other Linux build, so on other platforms
  Groundhog only supervises an executable that is already installed.

## Build

```
mvn package
```

This makes `target/groundhog-<version>-jar-with-dependencies.jar`.

## Run

```
java -jar target/groundhog-<version>-jar-with-dependencies.jar start
```

| Option | Meaning | Default |
| --- | --- | --- |
| `-hl`, `--hedgehoglocation` | Folder that holds `hedgehog.bin` | `~/.local/bin/` |
| `--hp`, `-hedgehogport` | p2p port passed on to Hedgehog | Hedgehog's own default |
| `-ui`, `--update-interval` | Minutes between checks for a new release, `0` turns updating off | `60` |

## Updates

Before it first starts Hedgehog, and then every update interval, Groundhog:

1. Downloads `SHA256SUMS` and its signature from the latest release on
   [GitHub](https://github.com/unigrid-project/hedgehog/releases), and checks the signature against the
   Unigrid Foundation release key that is built into the jar (fingerprint
   `A1CB 0037 B3B9 2D59 5FA1 536C 95A9 8E88 8B0B A5D9`).
2. Compares the checksum of the installed `hedgehog.bin` with the one listed for the Linux executable. If
   they match, nothing more happens.
3. Otherwise downloads that release, and requires both the checksum and the signature of the file to
   match.
4. Stops the running node, swaps the new file in with an atomic move, and starts the node again.

Some things to know:

- A failed check never touches the running node. It is logged and tried again at the next interval.
- An installed executable that is newer than the latest release, such as a development build, is kept.
  Going back a version can make Hedgehog set its spork data aside.
- If `hedgehog.bin` is missing at startup, Groundhog downloads it and retries every minute until it works.
- The folder must be writable by the user that runs Groundhog.

## License

Groundhog is released by the Unigrid Foundation and UGD Software AB under the addended GNU Affero General
Public License, version 3. See the headers of the source files.
