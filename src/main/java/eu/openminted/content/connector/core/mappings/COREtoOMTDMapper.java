package eu.openminted.content.connector.core.mappings;

import eu.openminted.content.connector.utils.language.LanguageUtils;
import eu.openminted.registry.domain.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import uk.ac.core.elasticsearch.entities.ElasticSearchArticleMetadata;
import uk.ac.core.elasticsearch.entities.ElasticSearchRepo;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author lucasanastasiou
 */
@Service
public class COREtoOMTDMapper {

    @Autowired
    LanguageUtils languageUtils;
    
    /**
     * Converts a CORE document to OMTD schema
     *
     * @param esam
     * @return
     */
    public DocumentMetadataRecord convertCOREtoOMTD(ElasticSearchArticleMetadata esam) {
        DocumentMetadataRecord documentMetadataRecord = new DocumentMetadataRecord();

        // -- HEADER
        MetadataHeaderInfo metadataHeaderInfo = new MetadataHeaderInfo();
        // -- -- metadata creation date <-- repository document metadata updated
        Long time = esam.getRepositoryDocument().getMetadataUpdated();
        Date date = new Date((long) time * 1000);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar xMLGregorianCalendar = null;
        try {
            xMLGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            metadataHeaderInfo.setMetadataCreationDate(xMLGregorianCalendar);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(COREtoOMTDMapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        // -- -- metadata creators <-- contributors
        List<PersonInfo> relatedPersons = new ArrayList<>();
        for (String contributor : esam.getContributors()) {
            PersonInfo relatedPerson = new PersonInfo();
            Name personName = new Name();
            personName.setValue(contributor);
            relatedPerson.getNames().add(personName);
            relatedPerson.setPersonIdentifiers(null);
            relatedPersons.add(relatedPerson);
        }
        metadataHeaderInfo.setMetadataCreators(relatedPersons);
//        // -- -- metadata languages <--
//        List<Language> languages = new ArrayList<Language>();
//        if (esam.getLanguage() != null) {
//            Language language = new Language();
//            language.setLanguageId(esam.getLanguage().getCode());
//            language.setLanguageTag(esam.getLanguage().getName());
//            languages.add(language);
//            metadataHeaderInfo.setMetadataLanguages(languages);
//        }
        // -- -- metadata last date updated <-- repository document metadata updated
        metadataHeaderInfo.setMetadataLastDateUpdated(xMLGregorianCalendar);
        // -- -- metadata record identifier <--
        MetadataIdentifier metadataIdentifier = new MetadataIdentifier();
        metadataIdentifier.setMetadataIdentifierSchemeName(MetadataIdentifierSchemeNameEnum.OTHER);
        metadataIdentifier.setSchemeURI("");
        metadataIdentifier.setValue(esam.getIdentifiers().get(0));
        metadataHeaderInfo.setMetadataRecordIdentifier(metadataIdentifier);
        metadataHeaderInfo.setRevision(null);
        // -- -- source of metadata record
        SourceOfMetadataRecord source = new SourceOfMetadataRecord();
        List<RepositoryName> repositoryNames = new ArrayList<>();
        RepositoryInfo relatedRepository = new RepositoryInfo();
        for (ElasticSearchRepo repo : esam.getRepositories()) {

            RepositoryName repositoryName = new RepositoryName();
            repositoryName.setLang(null);
            repositoryName.setValue(repo.getName());
            repositoryNames.add(repositoryName);
            relatedRepository.setRepositoryNames(repositoryNames);
            List<RepositoryIdentifier> repositoryIdentifiers = new ArrayList<>();
            RepositoryIdentifier repositoryIdentifier = new RepositoryIdentifier();
            repositoryIdentifier.setValue("" + repo.getId());
            repositoryIdentifier.setRepositoryIdentifierSchemeName(RepositoryIdentifierSchemeNameEnum.OTHER);
            repositoryIdentifiers.add(repositoryIdentifier);
            relatedRepository.setRepositoryIdentifiers(repositoryIdentifiers);
        }
        source.setCollectedFrom(relatedRepository);
        List<String> urls = esam.getUrls();
        if (urls != null && !urls.isEmpty()) {
            if (urls.get(0) != null && !urls.get(0).isEmpty()) {
                source.setSourceMetadataLink(esam.getUrls().get(0));
            }
        }
        metadataHeaderInfo.setSourceOfMetadataRecord(source);
        // -- --
        metadataHeaderInfo.setUserQuery("");

        // *** metadata header end
        documentMetadataRecord.setMetadataHeaderInfo(metadataHeaderInfo);
        // ***

        Document document = new Document();

        DocumentInfo documentInfo = new DocumentInfo();
        // -- -- abstract
        List<Abstract> abstracts = new ArrayList<>();
        Abstract dAbstract = new Abstract();
        dAbstract.setValue(esam.getDescription());
        abstracts.add(dAbstract);
        documentInfo.setAbstracts(abstracts);

        // -- -- authors
        List<PersonInfo> authors = new ArrayList<>();
        for (String cAuthor : esam.getAuthors()) {
            PersonInfo relatedPerson = new PersonInfo();
            Name personName = new Name();
            personName.setValue(cAuthor);
            relatedPerson.getNames().add(personName);
            authors.add(relatedPerson);
        }
        documentInfo.setAuthors(relatedPersons);

        // -- -- Conference
        documentInfo.setConference(null);

        // -- -- contributors
        List<ActorInfo> contributors = new ArrayList<>();
        for (String cContributor : esam.getContributors()) {
            ActorInfo contributor = new ActorInfo();
            PersonInfo relatedPerson = new PersonInfo();
            Name personName = new Name();
            personName.setValue(cContributor);
            relatedPerson.getNames().add(personName);
            contributor.setRelatedPerson(relatedPerson);
            contributor.setRelatedOrganization(null);
        }
        documentInfo.setContributors(contributors);

        // -- -- distribution info
        DocumentDistributionInfo documentDistributionInfo = new DocumentDistributionInfo();
        // -- -- -- access url
        String accessUrl = "https://core.ac.uk/display/" + esam.getId();
        List<String> accessURLs = new ArrayList<>();
        accessURLs.add(accessUrl);
        DistributionLoc distributionLoc = new DistributionLoc();
        distributionLoc.setDistributionLocation(accessUrl);
//        documentDistributionInfo.setAccessURLs(accessURLs);
        // -- -- -- attribution text
        List<AttributionText> attributionTexts = new ArrayList<>();
        AttributionText attributionText = new AttributionText();
        attributionText.setLang(null);
        attributionText.setValue(null);
        attributionTexts.add(attributionText);
        documentDistributionInfo.setAttributionTexts(attributionTexts);
        // -- -- -- availability end date
        documentDistributionInfo.setAvailabilityEndDate(null);
        // -- -- -- availability start date
        documentDistributionInfo.setAvailabilityStartDate(null);

        // -- -- -- encodings
//        List<CharacterEncodingEnum> encodings = new ArrayList<>();
//        encodings.add(CharacterEncodingEnum.UTF_8);
        documentDistributionInfo.setCharacterEncoding(CharacterEncodingEnum.UTF_8);
        // -- -- -- copyrights
        documentDistributionInfo.setCopyrightStatements(null);
        // -- -- -- distributions
//        List<DistributionMediumEnum> distributions = new ArrayList<>();
//        distributions.add(DistributionMediumEnum.DOWNLOADABLE);
//        distributions.add(DistributionMediumEnum.ACCESSIBLE_THROUGH_INTERFACE);
        distributionLoc.setDistributionMedium(DistributionMediumEnum.DOWNLOADABLE);
        // -- -- -- download urls
        List<String> dlUrls = new ArrayList<>();
        if (esam.getFullText() != null) {
            dlUrls.add("https://core.ac.uk/download/pdf/" + esam.getId() + ".pdf");
            dlUrls.add(esam.getFullTextIdentifier());
            distributionLoc.setDistributionLocation("https://core.ac.uk/download/pdf/" + esam.getId() + ".pdf");
            documentDistributionInfo.setDistributionLoc(distributionLoc);
//            documentDistributionInfo.setDownloadURLs(dlUrls);
        }

        if (esam.getPdfHashValue() != null && !esam.getPdfHashValue().isEmpty()) {
            String pdfHashKey = esam.getPdfHashValue();
            documentDistributionInfo.setHashkey(pdfHashKey);
        }

        // -- -- -- fee
        documentDistributionInfo.setFee(null);
        // -- -- -- fullText
        FullText2 fullText = new FullText2();
        fullText.setLang(null);
        String fullTextString = esam.getFullText();
        String fullTextStringEscaped = StringEscapeUtils.escapeXml(fullTextString);
        fullText.setValue(fullTextStringEscaped);
        documentDistributionInfo.setFullText(fullText);
        // -- -- -- mime types
//        List<DataFormatInfo> dataFormatInfos = new ArrayList<>();
        DataFormatInfo dataFormatInfo = new DataFormatInfo();
        dataFormatInfo.setMimeType(MimeTypeEnum.APPLICATION_PDF);
//        dataFormatInfos.add(dataFormatInfo);
        documentDistributionInfo.setDataFormatInfo(dataFormatInfo);
        // -- -- -- rights holders
        documentDistributionInfo.setRightsHolders(null);
        // -- -- -- rights info
        documentDistributionInfo.setRightsInfo(null);
        // -- -- -- sizes
        SizeInfo sizeInfo = new SizeInfo();
        sizeInfo.setSize("" + esam.getRepositoryDocument().getPdfSize());
        sizeInfo.setSizeUnit(SizeUnitEnum.BYTES);
        List<SizeInfo> sizeInfos = new ArrayList<>();
        sizeInfos.add(sizeInfo);
        documentDistributionInfo.setSizes(sizeInfos);
        // -- -- -- user types
        List<UserTypeEnum> userTypes = new ArrayList<>();
        userTypes.add(UserTypeEnum.ACADEMIC);
        documentDistributionInfo.setUserTypes(userTypes);

        // -- -- SET document distribution info
        List<DocumentDistributionInfo> documentDistributionInfos = new ArrayList<>();
        documentDistributionInfos.add(documentDistributionInfo);

        documentInfo.setDistributions(documentDistributionInfos);

        // -- languages
        List<Language> languages2 = new ArrayList<>();
        if (esam.getLanguage() != null) {
            Language language = new Language();
            language.setLanguageId(getOMTDLanguageCode(esam.getLanguage().getCode()));
            language.setLanguageTag(getOMTDLanguageTag(getOMTDLanguageCode(esam.getLanguage().getCode())));
            languages2.add(language);
            documentInfo.setDocumentLanguages(languages2);
        } else {

            Language language = new Language();
            language.setLanguageId("und");
            language.setLanguageTag("Undetermined");
            languages2.add(language);
            documentInfo.setDocumentLanguages(languages2);
        }
        if (esam.getFullText() != null) {
            documentInfo.setDocumentType(DocumentTypeEnum.WITH_FULL_TEXT);
        } else {
            documentInfo.setDocumentType(DocumentTypeEnum.BIBLIOGRAPHIC_RECORD_ONLY);
        }
        documentInfo.setEdition(null);
        documentInfo.setFundingProjects(null);
        // -- identifiers
        if (esam.getIdentifiers() != null) {
            List<PublicationIdentifier> pubIdentifiers = new ArrayList<>();
            for (String id : esam.getIdentifiers()) {
                PublicationIdentifier publicationIdentifier = new PublicationIdentifier();
                publicationIdentifier.setValue(id);
                pubIdentifiers.add(publicationIdentifier);
            }
            documentInfo.setIdentifiers(pubIdentifiers);
        }
        // -- inbook
        documentInfo.setInBook(null);
        // -- journal
        JournalInfo relatedJournal = new JournalInfo();
        //to be refactored
//        for (ElasticSearchJournal esamJ : esam.getJournals()) {
//            List<JournalIdentifier> jIds = new ArrayList<>();
//            for (String esamJournalIdentifier : esamJ.getIdentifiers()) {
//                JournalIdentifier jId = new JournalIdentifier();
//                jId.setValue(esamJournalIdentifier);
//                jIds.add(jId);
//            }
//            List<JournalTitle> jTitles = new ArrayList<>();
//            JournalTitle journalTitle = new JournalTitle();
//            journalTitle.setValue(esamJ.getTitle());
//            jTitles.add(journalTitle);
//            relatedJournal.setIdentifiers(jIds);
//            relatedJournal.setJournalTitles(jTitles);
//        }
        documentInfo.setJournal(relatedJournal);
        // -- keywords
        documentInfo.setKeywords(null);
        documentInfo.setPages(null);
        try {
            String datePublished = esam.getDatePublished();
            Date dateOfPublish = null;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            dateOfPublish = df.parse(datePublished);
            eu.openminted.registry.domain.Date omtdDate = new eu.openminted.registry.domain.Date();
            omtdDate.setDay(dateOfPublish.getDay());
            omtdDate.setMonth(dateOfPublish.getMonth());
            omtdDate.setYear(dateOfPublish.getYear());
            documentInfo.setPublicationDate(omtdDate);
        } catch (ParseException ex) {
            Logger.getLogger(COREtoOMTDMapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        // CORE has no explicit info about type of publication
        // setting everything as OTHER
        documentInfo.setPublicationType(PublicationTypeEnum.OTHER);

        // -- publisher
//        ActorInfo actorInfo = new ActorInfo();
        OrganizationInfo relatedOrganization = new OrganizationInfo();
        OrganizationName organizationName = new OrganizationName();
        organizationName.setValue(esam.getPublisher());
        List<OrganizationName> oNames = new ArrayList<>();
        oNames.add(organizationName);
        relatedOrganization.setOrganizationNames(oNames);
//        actorInfo.setRelatedOrganization(relatedOrganization);
        documentInfo.setPublisher(relatedOrganization);
//        documentInfo.setPublisher(actorInfo);
        // -- size
        documentInfo.setSeries(null);
        SizeInfo sizeInfo1 = new SizeInfo();
        sizeInfo.setSize("" + esam.getRepositoryDocument().getPdfSize());
        sizeInfo.setSizeUnit(SizeUnitEnum.BYTES);
        List<SizeInfo> sizeInfos1 = new ArrayList<>();
        sizeInfos.add(sizeInfo1);
        documentInfo.setSizes(sizeInfos1);
        // -- subjects
        if (esam.getSubjects() != null) {
            List<Subject> subjects = new ArrayList<>();
            for (String esamSubject : esam.getSubjects()) {
                Subject subject = new Subject();
                subject.setValue(esamSubject);
                subjects.add(subject);
            }
            documentInfo.setSubjects(subjects);
        }
        if (esam.getTitle() != null) {
            List<Title> titles = new ArrayList<>();
            Title title = new Title();
            title.setValue(esam.getTitle());
            titles.add(title);
            documentInfo.setTitles(titles);
        }
        documentInfo.setVolume(null);
        // set document info
        document.setPublication(documentInfo);

        AnnotatedDocumentInfo annotatedDocumentInfo = new AnnotatedDocumentInfo();
        AnnotationInfo annotationInfo = new AnnotationInfo();
        annotatedDocumentInfo.setAnnotationInfo(null);
        annotatedDocumentInfo.setRawPublication(null);
        // set annotation - is empty atm
        document.setAnnotatedPublication(annotatedDocumentInfo);

        documentMetadataRecord.setDocument(document);

        return documentMetadataRecord;
    }
    private static String getOMTDLanguageCode(String langCode){
       
        if (langCode.equalsIgnoreCase("zh-cn") || langCode.equalsIgnoreCase("zh-tw")){
            return "zh";
        }
        return langCode;
    }
    private String getOMTDLanguageTag(String langCode){
        return languageUtils.getLangCodeToName().get(langCode);
    }
}
