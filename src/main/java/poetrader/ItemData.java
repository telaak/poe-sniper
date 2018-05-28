package poetrader;

import javafx.beans.property.SimpleStringProperty;

public class ItemData {
    public String getItemName() {
        return itemName.get();
    }

    public SimpleStringProperty itemNameProperty() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    public String getCharacterName() {
        return characterName.get();
    }

    public SimpleStringProperty characterNameProperty() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName.set(characterName);
    }

    public String getBuyout() {
        return buyout.get();
    }

    public SimpleStringProperty buyoutProperty() {
        return buyout;
    }

    public void setBuyout(String buyout) {
        this.buyout.set(buyout);
    }

    private final SimpleStringProperty itemName;
    private final SimpleStringProperty characterName;
    private final SimpleStringProperty buyout;

    public ItemData(String iName, String cName, String buyout) {
        itemName = new SimpleStringProperty(iName);
        characterName = new SimpleStringProperty(cName);
        this.buyout = new SimpleStringProperty(buyout);
    }
}
