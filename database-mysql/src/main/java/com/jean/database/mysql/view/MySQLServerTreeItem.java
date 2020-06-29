package com.jean.database.mysql.view;

import com.jean.database.api.BaseTask;
import com.jean.database.api.TaskManger;
import com.jean.database.api.utils.DialogUtil;
import com.jean.database.api.utils.FxmlUtils;
import com.jean.database.api.utils.ImageUtils;
import com.jean.database.mysql.MySQLMetadataProvider;
import com.jean.database.mysql.MySQLObjectTabController;
import com.jean.database.sql.BaseDatabaseItem;
import com.jean.database.sql.meta.CatalogMetaData;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class MySQLServerTreeItem extends BaseDatabaseItem<String> {

    private final ContextMenu contextMenu;
    private MySQLObjectTabController objectTabController;

    public MySQLServerTreeItem(String value, MySQLMetadataProvider metadataProvider) {
        super(value, metadataProvider);
        this.contextMenu = this.createContextMenu();
        Callback<Class<?>, Object> factory = MySQLObjectTabController.getFactory(getValue());
        try {
            FxmlUtils.LoadFxmlResult fxmlResult = FxmlUtils.loadFxml("/fxml/mysql-object-tab.fxml", factory);
            objectTabController = (MySQLObjectTabController) fxmlResult.getController();
        } catch (IOException e) {
            DialogUtil.error(e);
        }
        this.setGraphic(ImageUtils.createImageView("/mysql/mysql.png"));
    }


    @Override
    public ContextMenu getContextMenu() {
        return this.contextMenu;
    }

    @Override
    public void doubleClick() {
        this.open();
    }

    @Override
    public void select() {
        if (objectTabController != null) {
            objectTabController.selected();
        }
    }

    @Override
    public void close() {
        super.close();
        setOpen(false);
    }

    private void open() {
        if (isOpen()) {
            return;
        }

        TaskManger.execute(new OpenServerTask());
    }

    private ContextMenu createContextMenu() {
        MenuItem open = new MenuItem("打开连接", ImageUtils.createImageView("/image/connect.png"));
        open.disableProperty().bind(openProperty());
        open.setOnAction(event -> open());

        MenuItem close = new MenuItem("关闭连接",ImageUtils.createImageView("/image/disconnect.png"));
        close.disableProperty().bind(openProperty().not());
        close.setOnAction(event -> close());

        MenuItem copy = new MenuItem("复制连接...");
        copy.setOnAction(event -> {
        });

        MenuItem delete = new MenuItem("删除连接", ImageUtils.createImageView("/image/delete.png"));
        delete.setOnAction(event -> {
        });

        MenuItem properties = new MenuItem("连接属性...");
        properties.setOnAction(event -> {
        });

        MenuItem create = new MenuItem("新建数据库...",ImageUtils.createImageView("/image/add.png"));
        create.setOnAction(event -> {
        });

        MenuItem commandLine = new MenuItem("命令行界面...");
        commandLine.setOnAction(event -> {
        });

        MenuItem executeSqlFile = new MenuItem("运行SQL文件...");
        executeSqlFile.setOnAction(event -> {
        });

        MenuItem dataTransform = new MenuItem("数据传输...");
        dataTransform.setOnAction(event -> {
        });

        MenuItem refresh = new MenuItem("刷新", ImageUtils.createImageView("/image/refresh.png"));
        refresh.disableProperty().bind(openProperty().not());
        refresh.setOnAction(event -> {
        });

        return new ContextMenu(open, close, new SeparatorMenuItem(),
                copy, delete, properties, new SeparatorMenuItem(),
                commandLine, executeSqlFile, dataTransform, new SeparatorMenuItem(),
                refresh);
    }


    private class OpenServerTask extends BaseTask<List<CatalogMetaData>> {

        @Override
        protected List<CatalogMetaData> call() throws Exception {
            return getMetadataProvider().getCatalogs();
        }


        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected void succeeded() {
            super.succeeded();
            MySQLServerTreeItem serverTreeItem = MySQLServerTreeItem.this;
            ObservableList children = serverTreeItem.getChildren();
            children.clear();
            List<CatalogMetaData> value = getValue();
            for (CatalogMetaData metaData : value) {
                TreeItem item = new MySQLCatalogTreeItem(metaData, getMetadataProvider(), objectTabController);
                serverTreeItem.getChildren().add(item);
            }
            serverTreeItem.setExpanded(true);
            serverTreeItem.setOpen(true);
        }
    }

}