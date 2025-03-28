import unittest
import tempfile
import os
from unittest.mock import patch, MagicMock, call

class TestEnrichTaxonomy(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        """Set up the global mock for NCBITaxa."""
        # Mocking the ete4 module and NCBITaxa class
        # Patch NCBITaxa globally for all tests in this class
        cls.ncbitaxa_patcher = patch("updateIPRScanWithTaxonomicLineage.NCBITaxa")
        cls.MockNCBITaxa = cls.ncbitaxa_patcher.start()

        # Configure the mock instance
        cls.mock_ncbi_taxa = MagicMock()
        cls.MockNCBITaxa.return_value = cls.mock_ncbi_taxa

        # mock_ncbi_taxa = MagicMock()
        # mock_ete4 = MagicMock()
        # with patch.dict('sys.modules', {'ete4': mock_ete4}):
        #     mock_ete4.NCBITaxa.return_value = mock_ncbi_taxa

        #mock get_lineage method
        def side_effect_func(value):
            if value == 1144317:
                return [1, 131567, 2, 1224, 28216, 80840, 80864, 12916, 2684926, 1144317]
            elif value == 9606:
                return [1, 131567, 2, 2759, 33154, 33208, 9604, 9605, 9606]
            elif value == 99999999999:
                return []
            else:
                return [value]
        cls.mock_ncbi_taxa.get_lineage.side_effect = side_effect_func

    @classmethod
    def tearDownClass(cls):
        # Stop the global patcher after all tests
        cls.ncbitaxa_patcher.stop()

    def setUp(self):
        """Temporary file setup (for each test)"""
        self.input_file = tempfile.NamedTemporaryFile(delete=False, mode='w', suffix='.xml')
        self.output_file = tempfile.NamedTemporaryFile(delete=False, mode='w', suffix='.xml')

        # Reset mock calls before each test
        self.mock_ncbi_taxa.reset_mock()

        # import the main script after patching and mocking and set it as a class attribute
        from updateIPRScanWithTaxonomicLineage import TaxonomyEnricher
        self.taxonomy_enricher = TaxonomyEnricher(taxadb=None)


    def tearDown(self):
        """Clean up temporary resources."""
        os.unlink(self.input_file.name)
        os.unlink(self.output_file.name)

    def write_input_file(self, content):
        """Helper function to write content to the input file."""
        with open(self.input_file.name, 'w') as f:
            f.write(content)

    def read_output_file(self):
        """Helper function to read content from the output file."""
        with open(self.output_file.name, 'r') as f:
            return f.read()

    def assert_xml_equal(self, expected_xml, actual_xml):
        """
        A helper function for asserting that two XML strings are equivalent.
        """
        from lxml import etree

        # Parse both XML strings
        actual_tree = etree.fromstring(actual_xml.encode("utf-8"))
        expected_tree = etree.fromstring(expected_xml.encode("utf-8"))

        # Convert both trees to canonical string representations
        actual_canonical = etree.tostring(actual_tree, pretty_print=True)
        expected_canonical = etree.tostring(expected_tree, pretty_print=True)

        # Assert that both canonical strings are equal
        self.assertEqual(
            actual_canonical.decode("utf-8").strip(),
            expected_canonical.decode("utf-8").strip(),
            "XML documents are not equivalent"
        )

    def test_single_xref_with_taxonomy(self):
            """Test a single xref element with a valid OX taxonomy."""
            content = '''
            <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
            <protein>
                <xref id="tr|J0U7L2|J0U7L2_9BURK" name="tr|J0U7L2|J0U7L2_9BURK Cytochrome c553 OS=Acidovorax sp. CF316 OX=1144317 GN=PMI14_03334 PE=4 SV=1"/>
            </protein>
            </protein-matches>
            '''.lstrip()
            self.write_input_file(content)
            self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)

            output = self.read_output_file()
            expected = '''
            <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
            <protein>
                <xref id="tr|J0U7L2|J0U7L2_9BURK" name="tr|J0U7L2|J0U7L2_9BURK Cytochrome c553 OS=Acidovorax sp. CF316 OX=1,131567,2,1224,28216,80840,80864,12916,2684926,1144317 GN=PMI14_03334 PE=4 SV=1"/>
            </protein>
            </protein-matches>
            '''.lstrip()

            self.assert_xml_equal(expected, output)
            self.mock_ncbi_taxa.get_lineage.assert_called_once_with(1144317)

    def test_multiple_xrefs_with_taxonomy(self):
        """Test multiple xref elements, each with a valid OX taxonomy."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|A0A123|SAMPLE1" name="Sample OS=Homo sapiens OX=9606 PE=4 SV=1"/>
            <xref id="tr|J0U7L2|SAMPLE2" name="Sample OS=Acidovorax sp. OX=1144317 PE=4 SV=1"/>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)

        output = self.read_output_file()
        expected = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|A0A123|SAMPLE1" name="Sample OS=Homo sapiens OX=1,131567,2,2759,33154,33208,9604,9605,9606 PE=4 SV=1"/>
            <xref id="tr|J0U7L2|SAMPLE2" name="Sample OS=Acidovorax sp. OX=1,131567,2,1224,28216,80840,80864,12916,2684926,1144317 PE=4 SV=1"/>
        </protein>
        </protein-matches>
        '''.lstrip()

        self.assert_xml_equal(expected, output)
        self.mock_ncbi_taxa.get_lineage.assert_has_calls([call(9606), call(1144317)], any_order=False)

    def test_xref_without_taxonomy(self):
        """Test an xref element with no OX taxonomy."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|NO_TAX|SAMPLE" name="Sample OS=Unknown Species GN=GENE123"/>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)

        output = self.read_output_file()
        self.assert_xml_equal(content, output)
        self.mock_ncbi_taxa.get_lineage.assert_not_called()

    def test_xref_without_name(self):
        """Test an xref element with no name attr."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|NO_TAX|SAMPLE"/>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)
        output = self.read_output_file()
        self.assert_xml_equal(content, output)
        self.mock_ncbi_taxa.get_lineage.assert_not_called()

    def test_no_xrefs(self):
        """Test a file with no xref elements."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <other>Some content</other>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)
        output = self.read_output_file()
        self.assert_xml_equal(content, output)
        self.mock_ncbi_taxa.get_lineage.assert_not_called()

    def test_invalid_taxonomy_id(self):
        """Test an xref element with an invalid OX taxonomy."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|INVALID|SAMPLE" name="Sample OS=Species OX=99999999999"/>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)
        output = self.read_output_file()
        self.assert_xml_equal(content, output)
        self.mock_ncbi_taxa.get_lineage.assert_called_once_with(99999999999)

    def test_multiline_name(self):
        """Test an xref element with a multi-line xref element."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|INVALID|SAMPLE" name="This is some long text spanning two lines\nsecond line OS=Species OX=9606"/>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)

        output = self.read_output_file()
        expected = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|INVALID|SAMPLE" name="This is some long text spanning two lines\nsecond line OS=Species OX=1,131567,2,2759,33154,33208,9604,9605,9606"/>
        </protein>
        </protein-matches>
        '''.lstrip()

        self.assert_xml_equal(expected, output)
        self.mock_ncbi_taxa.get_lineage.assert_called_once_with(9606)


    def test_multiple_proteins(self):
        """Test a single xref element with a valid OX taxonomy."""
        content = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|J0U7L2|J0U7L2_9BURK" name="tr|J0U7L2|J0U7L2_9BURK Cytochrome c553 OS=Acidovorax sp. CF316 OX=1144317 GN=PMI14_03334 PE=4 SV=1"/>
        </protein>
        <protein>
            <xref id="tr|A0A123|SAMPLE1" name="Sample OS=Homo sapiens OX=9606"/>
        </protein>
        </protein-matches>
        '''.lstrip()
        self.write_input_file(content)
        self.taxonomy_enricher.enrich_xref_name(self.input_file.name, self.output_file.name)

        output = self.read_output_file()
        expected = '''
        <?xml version='1.0' encoding='UTF-8'?><protein-matches xmlns="https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas" interproscan-version="5.73-104.0">
        <protein>
            <xref id="tr|J0U7L2|J0U7L2_9BURK" name="tr|J0U7L2|J0U7L2_9BURK Cytochrome c553 OS=Acidovorax sp. CF316 OX=1,131567,2,1224,28216,80840,80864,12916,2684926,1144317 GN=PMI14_03334 PE=4 SV=1"/>
        </protein>
        <protein>
            <xref id="tr|A0A123|SAMPLE1" name="Sample OS=Homo sapiens OX=1,131567,2,2759,33154,33208,9604,9605,9606"/>
        </protein>
        </protein-matches>
        '''.lstrip()

        self.assert_xml_equal(expected, output)
        self.mock_ncbi_taxa.get_lineage.assert_has_calls([call(1144317), call(9606)], any_order=False)

if __name__ == '__main__':
    unittest.main()
