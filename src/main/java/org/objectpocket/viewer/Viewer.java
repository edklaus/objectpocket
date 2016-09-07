/*
 * Copyright (C) 2015 Edmund Klaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectpocket.viewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectpocket.ObjectPocket;
import org.objectpocket.ObjectPocketBuilder;
import org.objectpocket.ObjectPocketImpl;
import org.objectpocket.example.Address;
import org.objectpocket.example.Person;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class Viewer {

    private JFrame viewerFrame;
    private JSplitPane splitPane;
    private JTable objectTable;
    private JTree classTree;

    private List<ObjectPocketImpl> objectPocketList = new ArrayList<ObjectPocketImpl>();
    private JScrollPane scrollPane;
    private JPanel panel;
    private JPanel filterPanel;
    private JTextField filterTextField;
    private JButton filterButton;
    private JLabel statusLabel;

    /**
     * Create the application.
     */
    public Viewer() {
        initialize();
    }

    public void show() {
        viewerFrame.setVisible(true);
    }

    /**
     * Add a ObjectPocket instance to the viewer.
     * 
     * @param objectPocket
     */
    public void addObjectPocket(ObjectPocket objectPocket) {
        objectPocketList.add((ObjectPocketImpl)objectPocket);
        updateTree();
        viewerFrame.revalidate();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        viewerFrame = new JFrame();
        viewerFrame.setTitle("ObjectPocketViewer");
        viewerFrame.setBounds(100, 100, 1427, 524);
        viewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.2);
        viewerFrame.getContentPane().add(splitPane, BorderLayout.CENTER);

        classTree = new JTree();
        classTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        classTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                updateTable();
            }
        });
        splitPane.setLeftComponent(classTree);

        panel = new JPanel();
        splitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout(0, 0));

        scrollPane = new JScrollPane();
        panel.add(scrollPane);
        objectTable = new JTable();
        objectTable.setAutoCreateRowSorter(true);
        objectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane.setViewportView(objectTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        filterPanel = new JPanel();
        panel.add(filterPanel, BorderLayout.NORTH);
        filterPanel.setLayout(new BorderLayout(0, 0));

        filterTextField = new JTextField();
        filterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    updateTable();
                }
            }
        });
        filterPanel.add(filterTextField, BorderLayout.CENTER);
        filterTextField.setColumns(10);

        filterButton = new JButton("apply filter");
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateTable();
            }
        });
        filterPanel.add(filterButton, BorderLayout.EAST);

        statusLabel = new JLabel("");
        panel.add(statusLabel, BorderLayout.SOUTH);

    }

    private void updateTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("OBJECT STORES");
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        for (ObjectPocketImpl objectPocketImpl : objectPocketList) {
            DefaultMutableTreeNode objectPocketNode = new DefaultMutableTreeNode(objectPocketImpl.getSource());
            rootNode.add(objectPocketNode);
            Set<String> availableTypes = objectPocketImpl.getAvailableTypes();
            for (String type : availableTypes) {
                DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type);
                objectPocketNode.add(typeNode);
            }
        }
        classTree.setModel(treeModel);
    }

    private void updateTable() {
        String selectedType = ((DefaultMutableTreeNode) classTree.getSelectionPath().getLastPathComponent()).toString();
        String selectedObjectPocket = ((DefaultMutableTreeNode) classTree.getSelectionPath().getPathComponent(
                classTree.getSelectionPath().getPathCount() - 2)).toString();
        ObjectPocketImpl objectPocket = null;
        for (ObjectPocketImpl objectPocketImpl : objectPocketList) {
            if (objectPocketImpl.getSource().equals(selectedObjectPocket)) {
                objectPocket = objectPocketImpl;
            }
        }
        if (objectPocket != null) {
            Map<String, Object> objectMap = objectPocket.getMapForType(selectedType);

            try {

                Class<?> clazz = Class.forName(selectedType);
                List<Field> allFields = FieldUtils.getAllFieldsList(clazz);
                List<Field> fields = new ArrayList<>();
                // filter fields
                for (Field field : allFields) {
                    if (!Modifier.isTransient(field.getModifiers()) && !field.getName().equals("id")) {
                        fields.add(field);
                    }
                }
                String[] columnNames = new String[fields.size() + 2];
                columnNames[0] = "";
                columnNames[1] = "id";
                int index = 2;
                for (Field f : fields) {
                    columnNames[index] = f.getName() + "(" + f.getType().getSimpleName() + ")";
                    index++;
                }

                // filter objects
                Map<String, Object> filteredObjects = filterObjects(objectMap, fields);

                String[][] rowData = new String[filteredObjects.size()][columnNames.length];
                index = 0;
                statusLabel.setText("object count: " + filteredObjects.size());
                for (String key : filteredObjects.keySet()) {
                    rowData[index][0] = "" + (index + 1);
                    rowData[index][1] = key;
                    for (int i = 0; i < fields.size(); i++) {
                        Field f = fields.get(i);
                        f.setAccessible(true);
                        try {
                            Object object = f.get(filteredObjects.get(key));
                            if (object != null) {
                                rowData[index][2 + i] = object.toString();
                            } else {
                                rowData[index][2 + i] = "null";
                            }
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    index++;
                }
                DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames);
                objectTable.setModel(tableModel);
                viewerFrame.revalidate();

            } catch (ClassNotFoundException e) {
                Logger.getAnonymousLogger().warning("Could not load class for type. " + selectedType);
                e.printStackTrace();

                // --- fallback
                String[] columnNames = { "", "id", "data" };
                String[][] rowData = new String[objectMap.size()][3];
                int index = 0;
                for (String key : objectMap.keySet()) {
                    rowData[index][0] = "" + (index + 1);
                    rowData[index][1] = key;
                    rowData[index][2] = objectMap.get(key).toString();
                    index++;
                }
                DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames);
                objectTable.setModel(tableModel);
                viewerFrame.revalidate();
            }
        }
        for (int i = 0; i < objectTable.getColumnModel().getColumnCount(); i++) {
            if (i == 0) {
                objectTable.getColumnModel().getColumn(i).setPreferredWidth(50);
            } else {
                objectTable.getColumnModel().getColumn(i).setPreferredWidth(200);
            }
        }
    }

    // TODO: Object references. e.g.
    // domain=DomainEntity(27839-34090-3409-34-298730)
    // TODO: !=, >, <
    // TODO: cleaner structure
    private Map<String, Object> filterObjects(Map<String, Object> objectMap, List<Field> fields) {
        String filterText = filterTextField.getText();
        String[] split = filterText.split("=");
        if (split.length == 2) {
            Map<String, Object> filteredObjects = new HashMap<String, Object>();
            String key = split[0];
            String value = split[1];
            for (Field field : fields) {
                if (field.getName().toLowerCase().contains(key.toLowerCase())) {
                    for (String id : objectMap.keySet()) {
                        try {
                            field.setAccessible(true);
                            Object object = field.get(objectMap.get(id));
                            if (value.equalsIgnoreCase("null")) {
                                if (object == null || object.toString().equals("null")) {
                                    filteredObjects.put(id, objectMap.get(id));
                                }
                            } else if (object.toString() != null
                                    && object.toString().toLowerCase().contains(value.toLowerCase())) {
                                filteredObjects.put(id, objectMap.get(id));
                            }
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
            // special case id
            if (key.equals("id")) {
                for (String id : objectMap.keySet()) {
                    if (value.equals(id)) {
                        filteredObjects.put(id, objectMap.get(id));
                    }
                }
            }
            return filteredObjects;
        }
        return objectMap;
    }
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) throws Exception {
        String directory = System.getProperty("user.dir") + "/temp_object_pocket";
        ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket(directory);

        Address address1 = new Address();
        address1.setCity("Karlsruhe");
        Address address2 = new Address();
        address2.setCity("Freiburg");
        
        objectPocket.add(address1);
        objectPocket.add(address2);
        
        Viewer viewer = new Viewer();
        viewer.addObjectPocket(objectPocket); 
        viewer.show();
    }

}
