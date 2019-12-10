package io.piveau.utils;

import io.piveau.rdf.TripleHash;
import io.piveau.vocabularies.Prefixes;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.Normalizer;

/**
 * Created by sim on 10.02.2017.
 */
public class JenaUtils {
    private static final Logger log = LoggerFactory.getLogger(JenaUtils.class);

    private static ModelExtract extractor = new ModelExtract(new StatementBoundaryBase() {
        @Override
        public boolean stopAt(Statement s) {
            return s.getPredicate().equals(FOAF.primaryTopic);
        }
    });

    private static final BigInteger M = new BigInteger("ffffffffffffffffffffffffffffffff", 16);

    private JenaUtils() {
    }

    public static Model extractResource(Resource resource) {
        try {
            Model d = extractor.extract(resource, resource.getModel());
            d.setNsPrefixes(Prefixes.DCATAP_PREFIXES);
            return d;
        } catch (Exception e) {
            log.error("model extraction", e);
            return null;
        }
    }

    public static Model read(byte[] content, Lang lang, String baseUri) {
        Model model = ModelFactory.createDefaultModel();
        RDFParserBuilder builder = RDFParser.create()
                .source(new ByteArrayInputStream(content))
                .lang(lang);
        if (baseUri != null) {
            builder.base(baseUri);
        }

        builder.parse(model);
        return model;
    }

    public static Model read(byte[] content, String contentType) {
        return read(content, mimeTypeToLang(contentType), null);
    }

    public static Model read(byte[] content, String contentType, String baseUri) {
        return read(content, mimeTypeToLang(contentType), baseUri);
    }

    public static Dataset readDataset(byte[] content, String baseUri) {
        Dataset dataset = DatasetFactory.create();
        RDFParserBuilder builder = RDFParser.create()
                .source(new ByteArrayInputStream(content))
                .lang(Lang.TRIG);
        if (baseUri != null) {
            builder.base(baseUri);
        }
        builder.parse(dataset);
        return dataset;
    }

    public static String write(Dataset dataset, Lang lang) {
        StringWriter writer = new StringWriter();
        RDFDataMgr.write(writer, dataset, lang);
        return writer.toString();
    }

    public static String write(Model model, Lang lang) {
        StringWriter writer = new StringWriter();
        RDFDataMgr.write(writer, model, lang);
        return writer.toString();
    }

    public static String write(Model model, String contentType) {
        return write(model, mimeTypeToLang(contentType));
    }

    public static String findIdentifier(Resource resource) {
        return findIdentifier(resource, false, false);
    }

    public static String findIdentifier(Resource resource, boolean removePrefix, boolean uriRefPrecedence) {
        if (uriRefPrecedence) {
            return identifierPrefix(resource.isURIResource() ? resource.getURI() : getIdentifierProperty(resource), removePrefix);
        } else {
            String identifier = getIdentifierProperty(resource);
            if (identifier == null && resource.isURIResource()) {
                identifier = resource.getURI();
            }
            return identifierPrefix(identifier, removePrefix);
        }

    }

    private static String identifierPrefix(String identifier, boolean removePrefix) {
        if (removePrefix && identifier != null) {
            int idx = identifier.lastIndexOf('/');
            return idx != -1 ? identifier.substring(idx + 1) : identifier;
        } else {
            return identifier;
        }
    }

    private static String getIdentifierProperty(Resource resource) {
        Statement statement = resource.getProperty(DCTerms.identifier);
        if (statement != null) {
            RDFNode obj = statement.getObject();
            if (obj.isLiteral()) {
                return obj.asLiteral().getString();
            } else if (obj.isURIResource()) {
                return  obj.asResource().getURI();
            }
        }
        return null;
    }

    public static Lang mimeTypeToLang(String dataMimeType) {
        Lang lang = Lang.NTRIPLES;
        if (dataMimeType != null) {
            int idx = dataMimeType.indexOf(';');
            String type = idx != -1 ? dataMimeType.substring(0, idx) : dataMimeType;
            switch (type.trim()) {
                case "application/rdf+xml":
                    lang = Lang.RDFXML;
                    break;
                case "application/ld+json":
                case "application/json":
                    lang = Lang.JSONLD;
                    break;
                case "text/turtle":
                    lang = Lang.TURTLE;
                    break;
                case "text/n3":
                    lang = Lang.N3;
                    break;
                case "application/trig":
                    lang = Lang.TRIG;
                    break;
                case "application/n-triples":
                    lang = Lang.NTRIPLES;
                    break;
                default:
            }
        }
        return lang;
    }

    public static String normalize(String id) {
        String normalized  = Normalizer.normalize(id, Normalizer.Form.NFKD);
        //remove all '%'
        //replace non-word-characters with '-'
        //then combine multiple '-' into one
        return normalized.replaceAll("%", "").replaceAll("\\W", "-").replaceAll("-+", "-").toLowerCase();
    }

    public static String canonicalHash(Model model) {
        if (model == null) {
            return null;
        }

        BigInteger totalHash = BigInteger.ZERO;
        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            Statement stm = it.nextStatement();

            BigInteger basicHash = new BigInteger(TripleHash.tripleHash(stm.asTriple()), 16);

            if (stm.getSubject().isAnon()) {
                StmtIterator objIt = model.listStatements(null, null, stm.getSubject());
                while (objIt.hasNext()) {
                    basicHash = combineHashes(basicHash, TripleHash.tripleHash(objIt.nextStatement().asTriple()));
                }
            }

            if (stm.getObject().isAnon()) {
                StmtIterator subjIt = stm.getObject().asResource().listProperties();
                while (subjIt.hasNext()) {
                    basicHash = combineHashes(basicHash, TripleHash.tripleHash(subjIt.nextStatement().asTriple()));
                }
            }

            totalHash = totalHash.add(basicHash);
            totalHash = totalHash.mod(M);
        }

        return totalHash.toString(16);
    }

    private static BigInteger combineHashes(BigInteger base, String hash) {
        BigInteger sum = base.add(new BigInteger(hash, 16));
        return sum.mod(M);
    }

}
