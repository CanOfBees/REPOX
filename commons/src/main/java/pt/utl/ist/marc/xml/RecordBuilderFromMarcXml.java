/*
 * RecordBuilder.java
 *
 * Created on 8 de Janeiro de 2003, 1:02
 */

package pt.utl.ist.marc.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.helpers.DefaultHandler;

import pt.utl.ist.marc.MarcField;
import pt.utl.ist.marc.MarcRecord;
import pt.utl.ist.marc.MarcSubfield;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that build a Marc Record Class from a xml DOM. Use
 * Record.fromDom(Document doc) instead of this class.
 * 
 * @author Nuno Freire
 */
public class RecordBuilderFromMarcXml extends DefaultHandler {
    protected MarcRecord       rec = null;
    protected List<MarcRecord> recs;

    /**
     * Creates a new instance of this class.
     */
    public RecordBuilderFromMarcXml() {
    }

    /**
     * @param dom
     * @return Record parsed from Node
     */
    public MarcRecord parseDom(Node dom) {
        recs = new ArrayList<MarcRecord>();
        if (dom instanceof Document) {
            dom = dom.getFirstChild();
        }
        if (dom.getNodeName().equals("collection"))
            parseCollection(dom);
        else if (dom.getNodeName().equals("record")) parseRecord(dom);
        return rec;
    }

    /**
     * @param n
     */
    protected void parseCollection(Node n) {
        int sz = n.getChildNodes().getLength();
        for (int idx = 0; idx < sz; idx++) {
            Node node = n.getChildNodes().item(idx);
            if (node.getNodeName().equals("record")) parseRecord(node);
        }
    }

    /**
     * @param n
     */
    protected void parseRecord(Node n) {
        rec = new MarcRecord();
        int sz = n.getChildNodes().getLength();
        for (int idx = 0; idx < sz; idx++) {
            Node node = n.getChildNodes().item(idx);
            if (node.getNodeName().equals("leader"))
                rec.setLeader(node.getFirstChild().getNodeValue());
            else if (node.getNodeName().equals("controlfield"))
                parseControlField(node);
            else if (node.getNodeName().equals("datafield")) parseDataField(node);
        }
        recs.add(rec);
    }

    /**
     * @param n
     */
    protected void parseControlField(Node n) {
        MarcField f = rec.addField(Integer.parseInt(n.getAttributes().getNamedItem("tag").getNodeValue()));
        if (n.getFirstChild() == null)
            f.setValue("");
        else
            f.setValue(n.getFirstChild().getNodeValue());
        if (f.getTag() == 001) {
            rec.setNc(f.getValue());
        }
    }

    /**
     * @param n
     */
    protected void parseDataField(Node n) {
        MarcField f = rec.addField(Integer.parseInt(n.getAttributes().getNamedItem("tag").getNodeValue()));

        if (n.getAttributes().getNamedItem("ind1") != null && n.getAttributes().getNamedItem("ind1").getNodeValue().length() > 0)
            f.setInd1(n.getAttributes().getNamedItem("ind1").getNodeValue().charAt(0));
        else
            f.setInd1(' ');
        if (n.getAttributes().getNamedItem("ind2") != null && n.getAttributes().getNamedItem("ind2").getNodeValue().length() > 0)
            f.setInd2(n.getAttributes().getNamedItem("ind2").getNodeValue().charAt(0));
        else
            f.setInd2(' ');

        int sz = n.getChildNodes().getLength();
        for (int idx = 0; idx < sz; idx++) {
            Node node = n.getChildNodes().item(idx);
            if (node.getNodeName().equals("subfield")) parseSubfield(node, f);
        }
    }

    /**
     * @param n
     * @param f
     */
    protected void parseSubfield(Node n, MarcField f) {
        MarcSubfield sf = f.addSubfield(n.getAttributes().getNamedItem("code").getNodeValue().charAt(0));
        if (n.getFirstChild() != null)
            sf.setValue(n.getFirstChild().getNodeValue());
        else
            sf.setValue("");
    }

    /**
     * @param dom
     * @return List of Records pased from a Document
     */
    public List<MarcRecord> parseDomGetRecords(Document dom) {
        recs = new ArrayList<MarcRecord>();
        int sz = dom.getChildNodes().getLength();
        for (int idx = 0; idx < sz; idx++) {
            Node node = dom.getChildNodes().item(idx);
            if (node.getNodeName().equals("collection")) parseCollection(node);
        }
        return recs;
    }

    //    public static Record domToRecord(Document dom){
    //        RecordBuilder bld=new RecordBuilder();
    //        return bld.parseDom(dom);
    //    }

    /**
     * @param dom
     * @return Record parsed from Node
     */
    public static MarcRecord domToRecord(Node dom) {
        RecordBuilderFromMarcXml bld = new RecordBuilderFromMarcXml();
        if (dom instanceof Document) return bld.parseDom(dom.getFirstChild());
        return bld.parseDom(dom);
    }

    /**
     * @param dom
     * @return List of Records parsed from a Document
     */
    public static List<MarcRecord> domToRecords(Document dom) {
        RecordBuilderFromMarcXml bld = new RecordBuilderFromMarcXml();
        return bld.parseDomGetRecords(dom);
    }
}
