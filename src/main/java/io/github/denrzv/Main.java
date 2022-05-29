package io.github.denrzv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {


        // Задание №1

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        Optional<List<Employee>> list = parseCSV(columnMapping, fileName);
        optionalToJson("data.json", list);

        //Задание №2

        fileName = "data.xml";
        list = parseXML(fileName);
        optionalToJson("data2.json", list);

        //Задание №3
        Optional<String> content = readString("data2.json");
        if (content.isPresent()) {
            list = Optional.ofNullable(jsonToList(content.get()));
            list.ifPresentOrElse(
                    System.out::println,
                    () -> System.err.println("Ошибка! Не удалось получить список объектов.")
            );
        } else {
            System.err.println("Ошибка! Не удалось прочитать файл.");
        }
    }

    public static List<Employee> jsonToList(String json) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, listType);
    }

    public static Optional<String> listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return Optional.ofNullable(gson.toJson(list, listType));
    }

    public static void optionalToJson(String fileName, Optional<List<Employee>> list) {
        if (list.isPresent()) {
            Optional<String> json = listToJson(list.get());
            json.ifPresentOrElse(
                    json_ -> writeString(json_, fileName),
                    () -> System.err.println("Ошибка! Не удалось сформировать json файл.")
            );
        } else {
            System.err.println("Ошибка! Не удалось получить список.");
        }
    }

    public static Optional<List<Employee>> parseCSV(String[] columnMapping, String fileName) {
        Optional<List<Employee>> list = Optional.empty();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setColumnMapping(columnMapping);
            strategy.setType(Employee.class);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            list = Optional.ofNullable(csv.parse());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public static Optional<List<Employee>> parseXML(String fileName) {
        Optional<List<Employee>> list = Optional.empty();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            NodeList nodeList = doc.getElementsByTagName("employee");
            List<Employee> employees = new ArrayList<>();

            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node_ = nodeList.item(i);
                if (Node.ELEMENT_NODE == node_.getNodeType()) {
                    Element element = (Element) node_;
                    NodeList nl = element.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++) {
                        Node node__ = nl.item(j);
                        if (Node.ELEMENT_NODE == node__.getNodeType()) {
                            map.put(nl.item(j).getNodeName(), nl.item(j).getTextContent());
                        }
                    }
                }

                employees.add(new Employee(Long.parseLong(map.get("id")), map.get("firstName"), map.get("lastName"),
                        map.get("country"), Integer.parseInt(map.get("age"))));
                list = Optional.of(employees);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public static Optional<String> readString(String fileName) {
        Optional<String> text = Optional.empty();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            text = Optional.ofNullable(bufferedReader.readLine());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return text;
    }

    public static void writeString(String str, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}