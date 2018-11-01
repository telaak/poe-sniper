# poe-sniper

Sniping tool for poe.trade searches. Uses poe.trade's search strings to listen to websocket messages. Automatically logs found items in a table and copies the most recent entry to your clipboard.

![Screenshot](https://laaksonen.me/poe-trader.png)

## Getting Started

### Prerequisites

```
Java 1.8+, JDK, JavaFX
```

### Installing

Clone or download the repository

```
git clone https://github.com/telaak/poe-sniper.git
```

Compile the .jar with IntelliJ IDEA 

```
IntelliJ: File->Project Structure->Artifacts->JavaFx Application
```
or with javafx-maven-plugin

```
mvn jfx:jar
```

## Running

```
java -jar poe_trader.jar
```

## Built With

* [IntelliJ IDEA](https://www.jetbrains.com/idea/) - The IDE used
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Teemu Laaksonen**

See also the list of [contributors](https://github.com/telaak/poe-sniper/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

* poe.trade - amazing search engine
