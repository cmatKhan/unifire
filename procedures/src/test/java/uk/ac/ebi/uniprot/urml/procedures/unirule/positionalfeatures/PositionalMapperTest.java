/*
 *  Copyright (c) 2018 European Molecular Biology Laboratory
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.ebi.uniprot.urml.procedures.unirule.positionalfeatures;

import uk.ac.ebi.uniprot.urml.procedures.unirule.TestUtils;

import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PositionalMapper}
 *
 * @author Alexandre Renaux
 */
public class PositionalMapperTest {

    @Test
    /* Based on UniRule prediction:
        UR000100891     FEATURE_BINDING Xanthine        A0A1F0RY62      156     156     0.0     0.0     UR      PREDICTED       OC:+:Bacteria;FRAGMENT:-;MATCH:+:MF_01184;FTPO:K=156;     */
    public void testSinglePositionTemplateMappingToSamePosition(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFVQEVHS");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 189, "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDVLKVDNFLNHQVDPDLMADLGKEVYRRFSNEPITKILTVESSGIAPAIATAMSFHKPLVFARKHKSLTLKDHLYTATVYSFTKKTSNEIAISRKFLSADDNVLIIDDFLANGQAVEGLMDIIDQAGATLSGVGIVIEKTFQKGRKLLDDKHIRVESLARIRAFEDGQVVFEPED");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 189, "MKLLEDRIKKDGQVIGTDVLKVDNFLNHQVDPDLMADLGKEVYRRFSNEPITKILTVESSGIAPAIATAMSFHKPLVFARKHKSLTLKDHLYTATVYSFTKKTSNEIAISRKFLSADDNVLIIDDFLANGQAVEGLMDIIDQAGATLSGVGIVIEKTFQKGRKLLDDKHIRVESLARIRAFEDGQVVFE-DNILSTTVFSFTKQREYNVVISKDYLTPNDKVVFIDDFLAYGNAAKGIIDLCRQAGAELVGMGFIIEKTFQHGRDGIEAEGVRCESLAMIESLDNCQIKLK");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "156", "156");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(156));
        assertThat(positionalMapping.getMappedEnd(), equalTo(156));
        assertThat(positionalMapping.getMappedSequence(), equalTo("K"));
    }

    @Test
    /* Based on UniRule prediction:
        UR000100891     FEATURE_BINDING Xanthine        D1W526  30      30      0.0     0.0     UR      PREDICTED      OC:+:Bacteria;FRAGMENT:-;MATCH:+:MF_01184;FTPO:[NT]=27;  */
    public void testSinglePositionTemplateMappingToOtherPosition(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFVQEVHS");


        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 189, "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFV");

        Protein targetProtein = TestUtils.createProtein("D1W526", "MQKMKTLIDRILKDGKCYPGGILKVDKFINHQMDPNLMKAIAIEFIKRYASTEINKILTIEASGIAPAIVMGLLLDLPVVFAKKKKPSTMDNILSTTVFSFTKQREYNVVISKDYLTPNDKVVFIDDFLAYGNAAKGIIDLCRQAGAELVGMGFIIEKTFQHGRDGIEAEGVRCESLAMIESLDNCQIKLKNIEK");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 4, 191, "MKTLIDRILKDGKCYPGGILKVDKFINHQMDPNLMKAIAIEFIKRYASTEINKILTIEASGIAPAIVMGLLLDLPVVFAKKKKPSTM-DNILSTTVFSFTKQREYNVVISKDYLTPNDKVVFIDDFLAYGNAAKGIIDLCRQAGAELVGMGFIIEKTFQHGRDGIEAEGVRCESLAMIESLDNCQIKLK");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "27", "27");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(30));
        assertThat(positionalMapping.getMappedEnd(), equalTo(30));
        assertThat(positionalMapping.getMappedSequence(), anyOf(equalTo("N"), equalTo("T")));
    }

    @Test
    /* Based on UniRule prediction:
        UR000100891     FEATURE_BINDING Xanthine        A0A150MDD4      41      41      0.0     0.0     UR      PREDICTED       OC:+:Bacteria;FRAGMENT:-;MATCH:+:MF_01184;FTPO:[NT]=27; */
    public void testSinglePositionTemplateMappingToOtherPosition_2(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFVQEVHS");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 189, "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFV");

        Protein targetProtein = TestUtils.createProtein("A0A150MDD4", "MAFLFSGIIQGGERMEKLIEKIRKEGTVLGEDILKVDRFLNHQIDPLFMDEIGREFARRFRDDRVTKVLTAESSGIAPAVMTGLHLGVPVVFARKKKSVTMTDALYSEKVFSFTKKSEYELSVSKKFLSADDRVLIIDDFLANGQAAGALVKITEKSGASLTGIGIVIEKSFQEGGSLLRKQGIRVESLVIIESLAGGVIHFADTKREVYAR");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 15, 203, "MEKLIEKIRKEGTVLGEDILKVDRFLNHQIDPLFMDEIGREFARRFRDDRVTKVLTAESSGIAPAVMTGLHLGVPVVFARKKKSVTMTDALYSEKVFSFTKKSEYELSVSKKFLSADDRVLIIDDFLANGQAAGALVKITEKSGASLTGIGIVIEKSFQEGGSLLRKQGIRVESLVIIESLAGGVIHFA-DNILSTTVFSFTKQREYNVVISKDYLTPNDKVVFIDDFLAYGNAAKGIIDLCRQAGAELVGMGFIIEKTFQHGRDGIEAEGVRCESLAMIESLDNCQIKLK");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "27", "27");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(41));
        assertThat(positionalMapping.getMappedEnd(), equalTo(41));
        assertThat(positionalMapping.getMappedSequence(), anyOf(equalTo("N"), equalTo("T")));
    }

    @Test
    /* Based on UniRule predictions:
        UR000100891     FEATURE_BINDING Xanthine        A0A0R2CFM5      48      48      0.0     0.0     UR      PREDICTED       OC:+:Bacteria;FRAGMENT:-;MATCH:+:MF_01184;FTPO:[NT]=27;
        UR000100891     FEATURE_BINDING Xanthine        A0A0R2CFM5      177     177     0.0     0.0     UR      PREDICTED       OC:+:Bacteria;FRAGMENT:-;MATCH:+:MF_01184;FTPO:K=156;     */
    public void testDoublePositionTemplateMappingToDifferentPositions(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFVQEVHS");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 189, "MEALKRKIEEEGVVLSDQVLKVDSFLNHQIDPLLMQRIGDEFASRFAKDGITKIVTIESSGIAPAVMTGLKLGVPVVFARKHKSLTLTDNLLTASVYSFTKQTESQIAVSGTHLSDQDHVLIIDDFLANGQAAHGLVSIVKQAGASIAGIGIVIEKSFQPGRDELVKLGYRVESLARIQSLEEGKVSFV");

        Protein targetProtein = Protein.builder().withId("A0A0R2CFM5").withSequence().withLength(217).withIsFragment(false).withValue("MASFTVFNCKRGFFILKGEKNVKLLEDRIREDGLVLPGNVLKVNQFLNHQIDPDLMYKLGQEFVSLFEGEKITKILTIEASGIAPAMMTGLILHVPVLFARKQKSVTMNDGLFTAEVYSYTKKVKNTVSVDQKFLSSDDKVLIIDDFLANGQAVQGLIEICKEAQAELVGVGIAIEKSFQDGAALIEKQGIKLESLARISSFDDNKVHFVGDETNE").end().build();
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 22, 210, "VKLLEDRIREDGLVLPGNVLKVNQFLNHQIDPDLMYKLGQEFVSLFEGEKITKILTIEASGIAPAMMTGLILHVPVLFARKQKSVTMNDGLFTAEVYSYTKKVKNTVSVDQKFLSSDDKVLIIDDFLANGQAVQGLIEICKEAQAELVGVGIAIEKSFQDGAALIEKQGIKLESLARISSFDDNKVHFV");

        PositionalMapping positionalMapping1 = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "27", "27");
        PositionalMapper.map(positionalMapping1);

        assertTrue(positionalMapping1.getIsValid());
        assertThat(positionalMapping1.getMappedStart(), equalTo(48));
        assertThat(positionalMapping1.getMappedEnd(), equalTo(48));
        assertThat(positionalMapping1.getMappedSequence(), anyOf(equalTo("N"), equalTo("T")));

        PositionalMapping positionalMapping2 = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "156", "156");
        PositionalMapper.map(positionalMapping2);

        assertTrue(positionalMapping2.getIsValid());
        assertThat(positionalMapping2.getMappedStart(), equalTo(177));
        assertThat(positionalMapping2.getMappedEnd(), equalTo(177));
        assertThat(positionalMapping2.getMappedSequence(), equalTo("K"));
    }

    @Test
    /* Based on UniRule prediction:
    UR000568546     FEATURE_REGION  Involved in apical transport and lipid raft association A0A0K0NHR1      11      33      0.0     0.0     UR      PREDICTED       FRAGMENT:-;MATCH:+:MF
_04071;OC:+:Orthomyxoviridae;FTPO:=11-33; */
    public void testSingleRangeTemplateMappingToSameRange(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_04071");

        TemplateProtein templateProtein = TestUtils.createProtein("P03468", "MNPNQKIITIGSICLVVGLISLILQIGNIISIWISHSIQTGSQNHTGICNQNIITYKNSTWVKDTTSVILTGNSSLCPIRGWAIYSKDNSIRIGSKGDVFVIREPFISCSHLECRTFFLTQGALLNDKHSNGTVKDRSPYRALMSCPVGEAPSPYNSRFESVAWSASACHDGMGWLTIGISGPDNGAVAVLKYNGIITETIKSWRKKILRTQESECACVNGSCFTIMTDGPSDGLASYKIFKIEKGKVTKSIELNAPNSHYEECSCYPDTGKVMCVCRDNWHGSNRPWVSFDQNLDYQIGYICSGVFGDNPRPEDGTGSCGPVYVDGANGVKGFSYRYGNGVWIGRTKSHSSRHGFEMIWDPNGWTETDSKFSVRQDVVAMTDWSGYSGSFVQHPELTGLDCMRPCFWVELIRGRPKEKTIWTSASSISFCGVNSDTVDWSWPDGAELPFSIDK");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 452, "MNPNQKIITIGSICLVVGLISLILQIGNIISIWISHSIQTGSQNHTGICNQNIITYKNSTWVKDTTSVILTGNS-----------------SLCPIRGWAIYSKDNSIRIGSKGDVFVIREPFISCSHLECRTFFLTQGALLNDKHSNGTVKDRSPYRALMSCPVGEAPSPYNSRFESVAWSASACHDGMGWLTIGISGPDNGAVAVLKYNGIITETIKSWRKKILRTQESECACVNGSCFTIMTDGPSDGLASYKIFKIEKGKVTKSIELNAPNSHYEECSCYPDTGKVMCVCRDNWHGSNRPWVSFD-QNLDYQIGYICSGVFGDNPRPEDG--TGSCGPVYVDG--ANGVKGFSYRYGNGVWIGRTKSHSSRHGFEMIWDPNGWTETDSKF-SVRQDVVAMTDWSGYSGSFVQHPELtgLDCMRPCFWVELIRGRPKEK--TIWTSASSISFCGVNSDTVDWSWPDGAELPFSI");

        Protein targetProtein = TestUtils.createProtein("A0A0K0NHR1", "MNPNQKIITIGSVSLTIATMCLFMQIAILITTVTLHFKQYECDSPANNQVMPCEPVIIEKNITKIVYVTNTTIEKEVCPKLGEYRNWSKPQCRITGFAPFSKDNSVRLSAGGAIWVTREPYVSCDPNKCYQFALGQGTTLDNKHSNDTIHDRTPYRTLLMNELGVPFHLGTRQVCIAWSSSSCHDGKAWLHVCVTGHDKNATASFIYDGKLVDSISSWSQNILRTQESECVCINGICTVVMTDGSASGKADTKILFIEEGKVIHISPLLGSAQHVEECSCYPRYPDVRCICRDNWKGSNRPIVDIRMKDYSISSSYMCSGLVGDTPRNNDGSSNSNCRNPNNERGNHGVKGWAFDDGNDTWMGRTINKDSRLGYETFKVVGGWSQPNSKLQVNRQVIVDSDNRSGYSGIFSVEGKSCINRCFYVELIRGRRQETRVWWTSNSIVVFCGTSGTYGSGSWPDGADINFMPI");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 468, "MNPNQKIITIGSVSLTIATMCLFMQIAILITTVTLHFKQY---ECDSPANNQVMPCEPVIIEKNITKIVYVTnTTIEKEVCPKLGEYRNWSKPQCRITGFAPFSKDNSVRLSAGGAIWVTREPYVSCDPNKCYQFALGQGTTLDNKHSNDTIHDRTPYRTLLMNELGVPFHLGT-RQVCIAWSSSSCHDGKAWLHVCVTGHDKNATASFIYDGKLVDSISSWSQNILRTQESECVCINGICTVVMTDGSASGKADTKILFIEEGKVIHISPLLGSAQHVEECSCYPRYPDVRCICRDNWKGSNRPIVDIRMKDYSISSSYMCSGLVGDTPRNNDGSSNSNCRNPN-NERGNHGVKGWAFDDGNDTWMGRTINKDSRLGYETFKVVGGWSQPNSKLQVNRQVIVDSDNRSGYSGIFSVEGKS--CINRCFYVELIRGRRQETR-VWWTSNSIVVFCGTSGTYGSGSWPDGADINFMP");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "11", "33");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(11));
        assertThat(positionalMapping.getMappedEnd(), equalTo(33));
    }

    @Test
    /* Based on UniRule prediction:
    UR000568546     FEATURE_DISULFID                A0A0K0NHR1      124     129     0.0     0.0     UR      PREDICTED       FRAGMENT:-;MATCH:+:MF_04071;OC:+:Orthomyxoviridae;FTPO:C-x*-C=109-114;
    UR000568546     FEATURE_DISULFID                A0A0K0NHR1      183     230     0.0     0.0     UR      PREDICTED       FRAGMENT:-;MATCH:+:MF_04071;OC:+:Orthomyxoviridae;FTPO:C-x*-C=169-216;
    UR000568546     FEATURE_DISULFID                A0A0K0NHR1      92      417     0.0     0.0     UR      PREDICTED
           FRAGMENT:-;MATCH:+:MF_04071;OC:+:Orthomyxoviridae;FTPO:C-x*-C=77-402; */
    public void testMultipleRangeTemplateMappingToDifferentRanges(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_04071");

        TemplateProtein templateProtein = TestUtils.createProtein("P03468", "MNPNQKIITIGSICLVVGLISLILQIGNIISIWISHSIQTGSQNHTGICNQNIITYKNSTWVKDTTSVILTGNSSLCPIRGWAIYSKDNSIRIGSKGDVFVIREPFISCSHLECRTFFLTQGALLNDKHSNGTVKDRSPYRALMSCPVGEAPSPYNSRFESVAWSASACHDGMGWLTIGISGPDNGAVAVLKYNGIITETIKSWRKKILRTQESECACVNGSCFTIMTDGPSDGLASYKIFKIEKGKVTKSIELNAPNSHYEECSCYPDTGKVMCVCRDNWHGSNRPWVSFDQNLDYQIGYICSGVFGDNPRPEDGTGSCGPVYVDGANGVKGFSYRYGNGVWIGRTKSHSSRHGFEMIWDPNGWTETDSKFSVRQDVVAMTDWSGYSGSFVQHPELTGLDCMRPCFWVELIRGRPKEKTIWTSASSISFCGVNSDTVDWSWPDGAELPFSIDK");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 452, "MNPNQKIITIGSICLVVGLISLILQIGNIISIWISHSIQTGSQNHTGICNQNIITYKNSTWVKDTTSVILTGNS-----------------SLCPIRGWAIYSKDNSIRIGSKGDVFVIREPFISCSHLECRTFFLTQGALLNDKHSNGTVKDRSPYRALMSCPVGEAPSPYNSRFESVAWSASACHDGMGWLTIGISGPDNGAVAVLKYNGIITETIKSWRKKILRTQESECACVNGSCFTIMTDGPSDGLASYKIFKIEKGKVTKSIELNAPNSHYEECSCYPDTGKVMCVCRDNWHGSNRPWVSFD-QNLDYQIGYICSGVFGDNPRPEDG--TGSCGPVYVDG--ANGVKGFSYRYGNGVWIGRTKSHSSRHGFEMIWDPNGWTETDSKF-SVRQDVVAMTDWSGYSGSFVQHPELtgLDCMRPCFWVELIRGRPKEK--TIWTSASSISFCGVNSDTVDWSWPDGAELPFSI");

        Protein targetProtein = TestUtils.createProtein("A0A0K0NHR1", "MNPNQKIITIGSVSLTIATMCLFMQIAILITTVTLHFKQYECDSPANNQVMPCEPVIIEKNITKIVYVTNTTIEKEVCPKLGEYRNWSKPQCRITGFAPFSKDNSVRLSAGGAIWVTREPYVSCDPNKCYQFALGQGTTLDNKHSNDTIHDRTPYRTLLMNELGVPFHLGTRQVCIAWSSSSCHDGKAWLHVCVTGHDKNATASFIYDGKLVDSISSWSQNILRTQESECVCINGICTVVMTDGSASGKADTKILFIEEGKVIHISPLLGSAQHVEECSCYPRYPDVRCICRDNWKGSNRPIVDIRMKDYSISSSYMCSGLVGDTPRNNDGSSNSNCRNPNNERGNHGVKGWAFDDGNDTWMGRTINKDSRLGYETFKVVGGWSQPNSKLQVNRQVIVDSDNRSGYSGIFSVEGKSCINRCFYVELIRGRRQETRVWWTSNSIVVFCGTSGTYGSGSWPDGADINFMPI");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 468, "MNPNQKIITIGSVSLTIATMCLFMQIAILITTVTLHFKQY---ECDSPANNQVMPCEPVIIEKNITKIVYVTnTTIEKEVCPKLGEYRNWSKPQCRITGFAPFSKDNSVRLSAGGAIWVTREPYVSCDPNKCYQFALGQGTTLDNKHSNDTIHDRTPYRTLLMNELGVPFHLGT-RQVCIAWSSSSCHDGKAWLHVCVTGHDKNATASFIYDGKLVDSISSWSQNILRTQESECVCINGICTVVMTDGSASGKADTKILFIEEGKVIHISPLLGSAQHVEECSCYPRYPDVRCICRDNWKGSNRPIVDIRMKDYSISSSYMCSGLVGDTPRNNDGSSNSNCRNPN-NERGNHGVKGWAFDDGNDTWMGRTINKDSRLGYETFKVVGGWSQPNSKLQVNRQVIVDSDNRSGYSGIFSVEGKS--CINRCFYVELIRGRRQETR-VWWTSNSIVVFCGTSGTYGSGSWPDGADINFMP");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "109", "114");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(124));
        assertThat(positionalMapping.getMappedEnd(), equalTo(129));
        assertTrue(positionalMapping.getMappedSequence().matches("C.*C"));

        PositionalMapping positionalMapping2 = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "169", "216");
        PositionalMapper.map(positionalMapping2);

        assertTrue(positionalMapping2.getIsValid());
        assertThat(positionalMapping2.getMappedStart(), equalTo(183));
        assertThat(positionalMapping2.getMappedEnd(), equalTo(230));
        assertTrue(positionalMapping2.getMappedSequence().matches("C.*C"));

        PositionalMapping positionalMapping3 = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "77", "402");
        PositionalMapper.map(positionalMapping3);

        assertTrue(positionalMapping3.getIsValid());
        assertThat(positionalMapping3.getMappedStart(), equalTo(92));
        assertThat(positionalMapping3.getMappedEnd(), equalTo(417));
        assertTrue(positionalMapping3.getMappedSequence().matches("C.*C"));
    }

    @Test
    /* Based on UniRule prediction:
    UR000568546     FEATURE_REGION  Head of neuraminidase   A0A0K0NHR1      91      469     0.0     0.0     UR      PREDICTED       FRAGMENT:-;MATCH:+:MF_04071;OC:+:Orthomyxoviridae;FTPO:=76-@CTER@;
     */
    public void testRangeTemplateMappingWithCTER(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_04071");

        TemplateProtein templateProtein = TestUtils.createProtein("P03468", "MNPNQKIITIGSICLVVGLISLILQIGNIISIWISHSIQTGSQNHTGICNQNIITYKNSTWVKDTTSVILTGNSSLCPIRGWAIYSKDNSIRIGSKGDVFVIREPFISCSHLECRTFFLTQGALLNDKHSNGTVKDRSPYRALMSCPVGEAPSPYNSRFESVAWSASACHDGMGWLTIGISGPDNGAVAVLKYNGIITETIKSWRKKILRTQESECACVNGSCFTIMTDGPSDGLASYKIFKIEKGKVTKSIELNAPNSHYEECSCYPDTGKVMCVCRDNWHGSNRPWVSFDQNLDYQIGYICSGVFGDNPRPEDGTGSCGPVYVDGANGVKGFSYRYGNGVWIGRTKSHSSRHGFEMIWDPNGWTETDSKFSVRQDVVAMTDWSGYSGSFVQHPELTGLDCMRPCFWVELIRGRPKEKTIWTSASSISFCGVNSDTVDWSWPDGAELPFSIDK");

        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 452, "MNPNQKIITIGSICLVVGLISLILQIGNIISIWISHSIQTGSQNHTGICNQNIITYKNSTWVKDTTSVILTGNS-----------------SLCPIRGWAIYSKDNSIRIGSKGDVFVIREPFISCSHLECRTFFLTQGALLNDKHSNGTVKDRSPYRALMSCPVGEAPSPYNSRFESVAWSASACHDGMGWLTIGISGPDNGAVAVLKYNGIITETIKSWRKKILRTQESECACVNGSCFTIMTDGPSDGLASYKIFKIEKGKVTKSIELNAPNSHYEECSCYPDTGKVMCVCRDNWHGSNRPWVSFD-QNLDYQIGYICSGVFGDNPRPEDG--TGSCGPVYVDG--ANGVKGFSYRYGNGVWIGRTKSHSSRHGFEMIWDPNGWTETDSKF-SVRQDVVAMTDWSGYSGSFVQHPELtgLDCMRPCFWVELIRGRPKEK--TIWTSASSISFCGVNSDTVDWSWPDGAELPFSI");

        Protein targetProtein = TestUtils.createProtein("A0A0K0NHR1", "MNPNQKIITIGSVSLTIATMCLFMQIAILITTVTLHFKQYECDSPANNQVMPCEPVIIEKNITKIVYVTNTTIEKEVCPKLGEYRNWSKPQCRITGFAPFSKDNSVRLSAGGAIWVTREPYVSCDPNKCYQFALGQGTTLDNKHSNDTIHDRTPYRTLLMNELGVPFHLGTRQVCIAWSSSSCHDGKAWLHVCVTGHDKNATASFIYDGKLVDSISSWSQNILRTQESECVCINGICTVVMTDGSASGKADTKILFIEEGKVIHISPLLGSAQHVEECSCYPRYPDVRCICRDNWKGSNRPIVDIRMKDYSISSSYMCSGLVGDTPRNNDGSSNSNCRNPNNERGNHGVKGWAFDDGNDTWMGRTINKDSRLGYETFKVVGGWSQPNSKLQVNRQVIVDSDNRSGYSGIFSVEGKSCINRCFYVELIRGRRQETRVWWTSNSIVVFCGTSGTYGSGSWPDGADINFMPI");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 468, "MNPNQKIITIGSVSLTIATMCLFMQIAILITTVTLHFKQY---ECDSPANNQVMPCEPVIIEKNITKIVYVTnTTIEKEVCPKLGEYRNWSKPQCRITGFAPFSKDNSVRLSAGGAIWVTREPYVSCDPNKCYQFALGQGTTLDNKHSNDTIHDRTPYRTLLMNELGVPFHLGT-RQVCIAWSSSSCHDGKAWLHVCVTGHDKNATASFIYDGKLVDSISSWSQNILRTQESECVCINGICTVVMTDGSASGKADTKILFIEEGKVIHISPLLGSAQHVEECSCYPRYPDVRCICRDNWKGSNRPIVDIRMKDYSISSSYMCSGLVGDTPRNNDGSSNSNCRNPN-NERGNHGVKGWAFDDGNDTWMGRTINKDSRLGYETFKVVGGWSQPNSKLQVNRQVIVDSDNRSGYSGIFSVEGKS--CINRCFYVELIRGRRQETR-VWWTSNSIVVFCGTSGTYGSGSWPDGADINFMP");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "76", "@CTER@");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(91));
        assertThat(positionalMapping.getMappedEnd(), equalTo(469));
    }

    @Test
    /* Based on UniRule prediction:
    UR000540784	FEATURE_REGION	RU A	W9WUY2	1	90	0.0	0.0	UR	PREDICTED	FRAGMENT:-;OC:+:Fungi;MATCH:+:MF_01989;FTPO:=@NTER@-103;
     */
    public void testRangeTemplateMappingWithNTER(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_04071");

        TemplateProtein templateProtein = TestUtils.createProtein("P58329", "MYHIDVFRIPCHSPGDTSGLEDLIETGRVAPADIVAVMGKTEGNGCVNDYTREYATAMLAACLGRHLQLPPHEVEKRVAFVMSGGTEGVLSPHHTVFARRPAIDAHRPAGKRLTLGIAFTRDFLPEEIGRHAQITETAGAVKRAMRDAGIASIDDLHFVQVKCPLLTPAKIASARSRGCAPVTTDTYESMGYSRGASALGIALATEEVPSSMLVDESVLNDWSLSSSLASASAGIELEHNVVIAIGMSEQATSELVIAHGVMSDAIDAASVRRTIESLGIRSDDEMDRIVNVFAKAEASPDGVVRGMRHTMLSDSDINSTRHARAVTGAAIASVVGHGMVYVSGGAEHQGPAGGGPFAVIARA");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 2, 363, "YHIDVFRIPCHSPGDTSGLEDLIETGRVAPADIVAVMGKTEGNGCVNDYTREYATAMLAACLGRHLQLPPHEVEKRVAFVMSGGTEGVLSPHHTVFARRPAIDAHRPAGKRLTLGIAFTRDFLPEEIGRHAQITETAGAVKRAMRDAGIASIDDLHFVQVKCPLLTPAKIASARSRGCAPVTTDTYESMGYSRGASALGIALATEEVPSSMLVDESVLNDWSLSSSLASASAGIELEHNVVIAIGMSEQATSELVIAHGVMSDAIDAASVRRTIESLGIRSDD----EMDRIVNVFAKAEASPDGVVRGMRHTMLSDSDINSTRHARAVTGAAIASVVGHGMVYVSGGAEHQGPAGGGPFAVIARA");

        Protein targetProtein = TestUtils.createProtein("W9WUY2", "MAAVEILKFPISSPADTTPLDRLKEAGYEASQILAVVGKTEGNGCVNDFSRTLASAVWEPRIPQDAVTIFSGGTEGVLSPHVTFFVRAHQGVATGLITAIGRTRVLAPHEVGTSAHSLQVAHTVSQMMQQASVTPAQVHLVLVKCPLLTSAKIEAIRAESRTPVTTDTYESMAKSRYASAVGIALALEELSHDMLEEASASQNIWSAKASCSSGAELEDCHVLVLATDPAGAGQQQGHLHAVSRYMADAIDASAVRDLLAQVESANGKVVQVFAKAQADPRGHVRSWRHTMNTDSDIHSTRHARAAVGGLIAGLVSDCEIYVSGGAEGQGPSGGGSLCMVYKT");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 343, "AAVEILKFPISSPADTTPLDRLKEAG-YEASQILAVVGKTEGNGCVNDFSRTLASAVWEPRIPQ-----------DAVTIFSGGTEGVLSPHVTFFVRAH------QgVATGLITAIGRTRVLAPHEVGTSAHSLQVAHTVSQMMQQASVT-PAQVHLVLVKCPLLTSAKIEAIRAESRTPVTTDTYESMAKSRYASAVGIALALEELSHDMLEEASASQNIW--SAKASCSSGAELEDCHVLVLATDPAGAgqqqGHLHAVSRYMADAIDASAVRDLLAQV----------EsaNGKVVQVFAKAQADPRGHVRSWRHTMNTDSDIHSTRHARAAVGGLIAGLVSDCEIYVSGGAEGQGPSGGGSLCMVYKT");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "@NTER@", "103");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(1));
        assertThat(positionalMapping.getMappedEnd(), equalTo(90));
    }


    @Test
    /* Based on UniRule prediction:
     UR000588304	FEATURE_ACT_SITE	Proton acceptor	A0A087HS97	65	65	0.0	0.0	UR	PREDICTED	MATCH:+:PS00920;OC:+:Eukaryota;FTPO:=5;
      */
    public void testSinglePositionMappingWithoutTemplate(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS00920");
        Protein targetProtein = TestUtils.createProtein("A0A087HS97", "MSGKEEMSSVKNTTPANGVAPSSIVRASIVQASTVYNNTPATLEKAEKLIAEAASNGSKLVVFPEAFIGGYPRGFRFGIGVGVHNEDGRDEFRNYHASAIRVPGPEVEKLAEVAGKNNVYLVMGAIEKDGYTLYCTALFFSSQGLFLGKHRKLMPTSLERCIWGYGDGSTIPVYDTPHGKLGAAICWENRMPLYRTALYAKGVEIYCAPTADGSKEWQSSMLHIALEGGCFVLSACQFCRRKDFPDHPDYLFTDWDDNQEDHAIVSQGGSVIISPLGQVLAGPNFESEGLVTADLDLGDVARAKLYFDVVGHYSKPEVFNLTVNEDRKKPVTFVSKVEKAEDEPKK");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 57, 72, "GskLVvFpEafIgGYP");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "5", "5");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(65));
        assertThat(positionalMapping.getMappedEnd(), equalTo(65));

    }


    @Test
    /* Based on UniRule prediction:
    UR000413441	FEATURE_DISULFID		D3DNU8	351	370	0.0	0.0	UR	PREDICTED	MATCH:+:PS51647;OC:+:Eutheria;FTPO:C-x*-C=80-99;
     */
    public void testRangeMappingWithoutTemplate(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS51647");
        Protein targetProtein = TestUtils.createProtein("D3DNU8", "MKLITILFLCSRLLLSLTQESQSEEIDCNDKDLFKAVDAALKKYNSQNQSNNQFVLYRITEATKTVGSDTFYSFKYEIKEGDCPVQSGKTWQDCEYKDAAKAATGECTATVGKRSSTKFSVATQ\n" +
                "TCQITPAEGPVVTAQYDCLGCVHPISTQSPDLEPILRHGIQYFNNNTQHSSLFTLNEVKRAQRQVVAGLNFRITYSIVQTNCSKENFLFLTPDCKSLWNGDTGECTDNAYIDIQLRIASFSQNCDIYPGKDFVQPPTKICVGCPRDIPTNSPELEETLTHTITKLNAENNATFYFKIDNVK\n" +
                "KARVQVVAGKKYFIDFVARETTCSKESNEELTESCETKKLGQSLDCNAEVYVVPWEKKIYPTVNCQPLGMISLMKRPPGFSPFRSSRIGEIKEETTSHLRSCEYKGRPPKAGAEPASEREVS");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 273, 376, "TNSPELEETLTHTITKLNAENNATFYFKIDNVKKARVQVVAGKKYFIDFVARETTCSKESNEELTE-SCETKKLGQSLDCNAEVYVVPWEKKIYPTVNCQPLGMI-DCKSLWNGDTGECTDNAYIDIQLRIASFSQNCDIYPGK");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "80", "99");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(351));
        assertThat(positionalMapping.getMappedEnd(), equalTo(370));
    }

    @Test
    /* Based on UniRule prediction:
    UR000400536	FEATURE_MOTIF	TFG box	A0A1D6LXV9	620	640	0.0	0.0	UR	PREDICTED	OC:+:Eukaryota;MATCH:+:PS51536;FTPO:=@FROM@-@TO@;
     */
    public void testRangeMappingWithoutTemplateWithFromToPlaceholders(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS51536");
        Protein targetProtein = TestUtils.createProtein("A0A1D6LXV9", "MAAAAAAAAAAPESYIGSLISLMSKSEIRYEGVLYTINTEESSIGLKNVRSFGTEGRKKDGQQIPASDKIYEYILFRGSDIKDLQVKSSPPSQSATLHNDPAIIQSHYPHPASLSTSLPSAAST\n" +
                "IGANPTSQNAPSLIQMPPPFQGNLPPYQSGTSLQSWNSAPMPSSANGTGLTMPPMYWPGYYTPPTGFSHLQPPLFLRPPHSLAVPQVLQLPVQYPGLGSLPAGFPNMPELPSFLQPGNSNSLNQTSGVPTSVSTPASLSTSQTESSRSQLPNKFSSDSASVFSVGFTPPSVTPSVSTVEPS\n" +
                "IPVSAVLPSLVNSKPVALPDSTMPSLSTAKPVIVPDASVLTYLSSQPPSANDVSPVNAAEQVPLVTPGQLLSTTSSTIISSHALQTASAVDLSSKAAPSTVPSSQATLFEGSTTQVASSSILSQQVAMTNESKVAKQREWKAKQTVVGRPGNKEPLLPEPKSVFQKPVGVSSYVRYNSRGR\n" +
                "GRGRGRGVCFSENMFCPTNFENQEVFEISDPGLPLFGYFTGQIFEEMSGQSHPITKFTEDFDFMAMNEKFNKDEVWGHLGKRIGQLNDEPNGYEDDVIEDDEISPRKPEAKAVYVKDDFFDSLSCNQIDNGGRNGRVKFSEQRKIDTETFGDSARHRPMGIRGRGPRGGARGRGYYGIRGY\n" +
                "GYTGRGRGYSYPNHQP");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 620, 640, "NGRVKFSEQRKIDTETFGDSA");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "@FROM@", "@TO@");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedStart(), equalTo(620));
        assertThat(positionalMapping.getMappedEnd(), equalTo(640));
    }

    @Test
    public void testMappedPositionsFromConditionsOutsideTargetAlignmentIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", "MKLITILFLCSRLLLSLTQESQ");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 18, "LITILFLCSRLLLSLTQE");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "2", "20");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testMappedPositionsFromConditionsLessThan1IsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", "MKLITILFLCSRLLLSLTQESQ");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 18, "LITILFLCSRLLLSLTQE");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "0", "0");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testTargetAlignmentPositionsOutsideSequenceLengthIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", "MKLITILFLCSRLLLSLTQESQ");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 23, 41, "LITILFLCSRLLLSLTQE");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "2", "18");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testEmptyTargetAlignmentIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", "MKLITILFLCSRLLLSLTQESQ");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 18, "");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "2", "18");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testNullTargetAlignmentIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", "MKLITILFLCSRLLLSLTQESQ");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 18, null);
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "2", "18");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testEmptyTargetSequenceIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", "");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 18, "MKLITILFLCSRLLLSLTQESQ");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "2", "18");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testNullTargetSequenceIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.PROSITE, "PS12345");
        Protein targetProtein = TestUtils.createProtein("P12345", null);
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 2, 18, "MKLITILFLCSRLLLSLTQESQ");
        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, null, "2", "18");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testWithTemplateMappingAndMappedPositionsFromConditionsOutsideTargetAlignmentIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "2", "20");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testWithTemplateMappingAndMappedPositionsFromConditionsLessThan1IsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "0", "0");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testWithTemplateMappingAndTargetAlignementPositionLessThan1IsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 0, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testWithTemplateMappingAndTemplateAlignementPositionLessThan1IsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 0, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testTemplateAlignmentPositionsOutsideSequenceLengthIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 15, 34, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testWithEmptyTemplateSequenceIsValid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), equalTo("MKLLEDRIKKDGQVIGTDV"));
        assertThat(positionalMapping.getMappedStart(), equalTo(1));
        assertThat(positionalMapping.getMappedEnd(), equalTo(19));
    }

    @Test
    public void testWithNullTemplateSequenceIsValid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", null);
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "MEALKRKIEEEGVVLSDQV");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertTrue(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), equalTo("MKLLEDRIKKDGQVIGTDV"));
        assertThat(positionalMapping.getMappedStart(), equalTo(1));
        assertThat(positionalMapping.getMappedEnd(), equalTo(19));
    }

    @Test
    public void testWithEmptyTemplateSequenceAlignmentIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, "");

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

    @Test
    public void testWithNullTemplateSequenceAlignmentIsInvalid(){
        Signature signature = TestUtils.createSignature(SignatureType.HAMAP, "MF_01184");

        TemplateProtein templateProtein = TestUtils.createProtein("P42085", "MEALKRKIEEEGVVLSDQV");
        PositionalProteinSignature templateMatch = TestUtils
                .createPositionalProteinSignature(templateProtein, signature, 1, 19, null);

        Protein targetProtein = TestUtils.createProtein("A0A1F0RY62", "MKLLEDRIKKDGQVIGTDV");
        PositionalProteinSignature targetMatch = TestUtils
                .createPositionalProteinSignature(targetProtein, signature, 1, 19, "MKLLEDRIKKDGQVIGTDV");

        PositionalMapping positionalMapping = TestUtils
                .createUnmappedPositionalMapping(targetMatch, templateMatch, "1", "19");
        PositionalMapper.map(positionalMapping);

        assertTrue(positionalMapping.getIsMapped());
        assertFalse(positionalMapping.getIsValid());
        assertThat(positionalMapping.getMappedSequence(), is(nullValue()));
        assertThat(positionalMapping.getMappedStart(), is(nullValue()));
        assertThat(positionalMapping.getMappedEnd(), is(nullValue()));
    }

}
