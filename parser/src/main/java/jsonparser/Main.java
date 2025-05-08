package jsonparser;

public class Main {
    public static void main(String[] args) throws Exception {
        Parser p = new Parser();
        p.parseJSONString("{\"name\":\"bob\", \"age\":12, \"obj\":[[4,5,{\"steve\":\"bob\"}],2,3]}");

    }

}