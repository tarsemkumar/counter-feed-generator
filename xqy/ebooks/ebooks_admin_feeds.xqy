xquery version '1.0-ml';
module namespace admin = "admin";
declare namespace meta = "http://nature.com/meta";
declare namespace pc = "http://www.palgraveconnect.com/ebook";
declare function admin:searchTitle($identifier as xs:string)
{
let $document := (cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),
cts:or-query((cts:element-value-query(xs:QName("meta:isbn13"), $identifier),
cts:element-value-query(xs:QName("meta:doi"), $identifier)))))[1]
return
<result>
{
if($document) then
<item>
{
<doi>{$document/record/metadata/meta:doi/fn:string()}</doi>,
<pcode>{$document/record/metadata/meta:pcode/fn:string()}</pcode>,
<title>{$document/record/metadata/meta:atitle/fn:string()}</title>,
<subtitle>{$document/record/metadata/meta:subtitle/fn:string()}</subtitle>,
<thirteen-digit-isbn>{$document/record/metadata/meta:isbns/meta:isbn13/fn:string()}</thirteen-digit-isbn>,
<collection>
<collection-name>{$document/record/metadata/meta:palgrave-connect/meta:collection-name/fn:string()}</collection-name>
<collection-acronym>{$document/record/metadata/meta:palgrave-connect/meta:collection-acronym/fn:string()}</collection-acronym>
<collection-workid>{$document/record/metadata/meta:palgrave-connect/meta:collection-work-id/fn:string()}</collection-workid>
</collection>,
<publication-date>{$document/record/metadata/meta:pdates/meta:pdate/fn:string()}</publication-date>,
<upload-date>{$document/record/metadata/meta:pdates/meta:first-online-date/fn:string()}</upload-date>,
<series-title>{$document/record/metadata/meta:series-title/fn:string()}</series-title>,
<edition-statement>{$document/record/metadata/meta:palgrave-connect/meta:edition-statement/fn:string()}</edition-statement>,
<catalogue-description>{$document/record/content/pc:ebook/pc:Product/pc:OtherText[pc:TextTypeCode eq '02']/pc:Text/fn:string()}</catalogue-description>,
<jacket-description>{$document/record/content/pc:ebook/pc:Product/pc:OtherText[pc:TextTypeCode eq '03']/pc:Text/fn:string()}</jacket-description>,
<hardback-isbn>{fn:string-join($document/record/metadata/meta:isbns/meta:hardback-isbns/meta:hardback-isbn, "; ")}</hardback-isbn>,
<paperback-isbn>{fn:string-join($document/record/metadata/meta:isbns/meta:paperback-isbns/meta:paperback-isbn, "; ")}</paperback-isbn>,
<epub-isbn>{$document/record/metadata/meta:isbns/meta:epub-isbn/fn:string()}</epub-isbn>,
<last-updated-date>{$document/record/metadata/meta:pdates/meta:last-ingest-date/fn:string()}</last-updated-date>,
<series-subject>{fn:string-join($document/record/metadata/meta:series-subjects/meta:series-subject, "; ")}</series-subject>,
<subject-code>{fn:string-join($document/record/metadata/meta:palgrave-connect/meta:palgrave-connect-subjects/meta:palgrave-connect-subject/@code, "; ")}</subject-code>,
<subject-text>{fn:string-join($document/record/metadata/meta:palgrave-connect/meta:palgrave-connect-subjects/meta:palgrave-connect-subject, "; ")}</subject-text>,
<platformUKPriceAmount>{$document/record/content/pc:ebook/pc:CustomConnect/pc:PlatformUKPriceAmount/fn:string()}</platformUKPriceAmount>,
<platformUSPriceAmount>{$document/record/content/pc:ebook/pc:CustomConnect/pc:PlatformUSPriceAmount/fn:string()}</platformUSPriceAmount>,
<profitCenter>{$document/record/content/pc:ebook/pc:CustomConnect/pc:ProfitCentre/fn:string()}</profitCenter>,
<vistaSection>{$document/record/content/pc:ebook/pc:CustomConnect/pc:VistaSection/fn:string()}</vistaSection>,
<yearFirstPublished>{$document/record/content/pc:ebook/pc:Product/pc:YearFirstPublished/fn:string()}</yearFirstPublished>,
<seeSawStatus>{$document/record/content/pc:ebook/pc:CustomConnect/pc:SeeSawStatus[1]/fn:string()}</seeSawStatus>,
<number-of-pages>{$document/record/content/pc:ebook/pc:Product/pc:NumberOfPages/fn:string()}</number-of-pages>,
<purchase-copy-url>{$document/record/content/pc:ebook/pc:Product/pc:ProductWebsite[pc:WebsiteRole eq '18']/pc:ProductWebsiteLink/fn:string()}</purchase-copy-url>,
<table-of-contents>{$document/record/content/pc:ebook/pc:Product/pc:OtherText[pc:TextTypeCode eq '04']/pc:Text/fn:string()}</table-of-contents>,
<author-biography>{$document/record/content/pc:ebook/pc:Product/pc:OtherText[pc:TextTypeCode eq '13']/pc:Text/fn:string()}</author-biography>,
<reviews>{$document/record/content/pc:ebook/pc:Product/pc:OtherText[pc:TextTypeCode eq '08']/pc:Text/fn:string()}</reviews>,
<contributor-list>{$document/record/content/pc:ebook/pc:Product/pc:PromotionCampaign/fn:string()}</contributor-list>,
<contributors>
{
for $contributor in $document/record/content/pc:ebook/pc:Product/pc:Contributor
return $contributor
}
</contributors>
}
</item>
else ()
}
</result>
};
declare function admin:getBookXmlUsingDoi($doi as xs:string)
{
(fn:collection(("PalgraveConnect", "palgrave_connect_pivot", "palgraveconnect_chapter"))/record[.//meta:doi eq $doi])[1]
};
declare function admin:getTitles($search-criteria-str as xs:string)
{
<results>{
let $critDoc := xdmp:unquote($search-criteria-str)
let $time-limit := xdmp:set-request-time-limit(600)
let $search-qname := if (fn:not($critDoc/title-search-criteria/groupcode)) then 'meta:pcode' else 'meta:collection-work-id'
let $value := if ($critDoc/title-search-criteria/pcode ne "") then $critDoc/title-search-criteria/pcode else $critDoc/title-search-criteria/groupcode
let $documents := if(fn:not($critDoc/title-search-criteria/pcode) and fn:not($critDoc/title-search-criteria/groupcode))
then fn:collection(("PalgraveConnect", "palgrave_connect_pivot"))
else cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),cts:element-value-query(xs:QName($search-qname), $value))
for $document in $documents
return
<item>{
<title>{$document/record/metadata/meta:atitle/fn:string()}</title>,
<subtitle>{$document/record/metadata/meta:subtitle/fn:string()}</subtitle>,
<thirteen-digit-isbn>{$document/record/metadata/meta:isbns/meta:isbn13/fn:string()}</thirteen-digit-isbn>,
<collection>
<collection-name>{$document/record/metadata/meta:palgrave-connect/meta:collection-name/fn:string()}</collection-name>
</collection>,
<publication-date>{$document/record/metadata/meta:pdates/meta:pdate/fn:string()}</publication-date>,
<upload-date>{$document/record/metadata/meta:pdates/meta:first-online-date/fn:string()}</upload-date>,
<series-title>{$document/record/metadata/meta:series-title/fn:string()}</series-title>,
<edition-statement>{$document/record/metadata/meta:palgrave-connect/meta:edition-statement/fn:string()}</edition-statement>,
<contributors>
{
for $contributor in $document/record/content/pc:ebook/pc:Product/pc:Contributor
return $contributor
}
</contributors>
}</item>
}</results>
};
declare function admin:getUploadedTitleByIsbns($isbnList as xs:string)
{
<Results>
{
let $time-limit := xdmp:set-request-time-limit(60)
let $search-documents := cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")), cts:element-value-query(xs:QName("meta:isbn13"), fn:tokenize($isbnList, ",")))
for $doc in $search-documents
return
<file pcode="{$doc/record/metadata/meta:pcode/fn:string()}">
<isbn>{$doc/record/metadata/meta:isbns/meta:isbn13/fn:string()}</isbn>
{
for $epub in $doc/record/content/pc:ebook/pc:Product/pc:RelatedProduct[pc:ProductForm eq 'DG']/pc:ProductIdentifier[pc:ProductIDType eq '15']/pc:IDValue/fn:string()
return <epub>{ $epub }</epub>
}
{
for $epub in $doc/record/content/pc:ebook/pc:Product/pc:ContentItem[pc:TextItem/pc:TextItemType ne "01"]/pc:MediaFile[pc:MediaFileTypeCode eq '01'][pc:MediaFileFormatCode eq 'EP']/pc:MediaFileLink/fn:string()
return <epub>{ $epub }</epub>
}
{
for $pdf in $doc//pc:CustomConnect/pc:ProductWebsite[pc:ProductWebsiteDescription eq 'E-Book File' or pc:ProductWebsiteDescription eq 'E-book File']/pc:ProductWebsiteLink/fn:string()
return <pdf>{ $pdf }</pdf>
}
{
for $chpaterPdf in $doc//pc:Product/pc:ContentItem[pc:TextItem/pc:TextItemType ne "01"]/pc:MediaFile[pc:MediaFileTypeCode eq '01'][pc:MediaFileFormatCode eq '04']/pc:MediaFileLink/fn:string()
return <pdf>{ $chpaterPdf }</pdf>
}
{
for $xls in $doc//pc:CustomConnect/pc:ProductWebsite[pc:ProductWebsiteDescription eq 'Data File']/pc:ProductWebsiteLink/fn:string()
return <xls>{ $xls }</xls>
}
{
for $xls in $doc//pc:Product/pc:ContentItem[pc:TextItem/pc:TextItemType ne "01"]/pc:MediaFile[pc:MediaFileTypeCode eq '01'][pc:MediaFileFormatCode eq 'EX']/pc:MediaFileLink/fn:string()
return <xls>{ $xls }</xls>
}
<imprint-name>{$doc/record/content/pc:ebook/pc:Product/pc:Imprint/pc:ImprintName/fn:string()}</imprint-name>
<ml-collection-name>{fn:string-join(xdmp:document-get-collections(xdmp:node-uri($doc)), ',') }</ml-collection-name>
{
for $chpaterIsbns in $doc//pc:Product/pc:ContentItem[pc:TextItem/pc:TextItemType eq "03"][pc:MediaFile/pc:MediaFileTypeCode eq '01']/pc:TextItem/pc:TextItemIdentifier[pc:TextItemIDType eq "06"]//pc:IDValue/fn:string()
return <actual-chapter-isbn>{ fn:substring($chpaterIsbns,9) }</actual-chapter-isbn>
}
</file>
}
</Results>
};
declare function admin:getFiles($pcode as xs:string)
{
<Results>{
let $time-limit := xdmp:set-request-time-limit(60)
for $doc in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),cts:element-value-query(xs:QName("meta:pcode"), $pcode))
return
<item isbn="{$doc/record/metadata/meta:isbns/meta:isbn13/fn:string()}">{
for $file-name in $doc//pc:CustomConnect/pc:ProductWebsite[pc:ProductWebsiteDescription eq 'E-Book File']/pc:ProductWebsiteLink/fn:string()
return <file>{ $file-name }</file>
}</item>
}</Results>
};
declare function admin:getEmptyPages($pcode as xs:string)
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
let $documents := cts:search(fn:collection('PalgraveConnect'), cts:element-value-query(xs:QName('meta:pcode'), $pcode))
for $doc in $documents
for $page in $doc//pc:page where fn:empty($page/fn:string())
return
if($doc//meta:has-chapter/fn:string() eq "false") then
<item isbn="{$doc/record/metadata/meta:isbns/meta:isbn13/fn:string()}"
file="{$doc//pc:CustomConnect/pc:ProductWebsite[pc:ProductWebsiteDescription eq 'E-Book File']/pc:ProductWebsiteLink/fn:string()}"
page="{$page/@id/fn:string}"></item>
else
<item isbn="{$doc/record/metadata/meta:isbns/meta:isbn13/fn:string()}"
file="{$page/../@file-name/fn:string()}"
page="{$page/@id}/fn:string()"></item>
} </Results>
};
declare function admin:getSubjects()
{
<Results> {
for $subjectMaAndMiTest in cts:element-values(xs:QName("meta:palgrave-connect-subject"),"",(), cts:collection-query("PalgraveConnect"))
let $tokens := fn:tokenize($subjectMaAndMiTest, "/")
let $subjectMacro := $tokens[1]
let $subjectMicro := $tokens[2]
return <item>
<main> { $subjectMacro } </main>
<sub> { $subjectMicro } </sub>
</item>
} </Results>
};
declare function admin:getSubjectSeries()
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
for $d in cts:element-value-co-occurrences(xs:QName("meta:series-subject"),xs:QName("meta:series-title"), (), cts:collection-query("PalgraveConnect"))
return
<item><main> { $d//cts:value[1]/fn:string() } </main>
<sub> { $d//cts:value[2]/fn:string() } </sub></item>
} </Results>
};
declare function admin:getIsbnsByCollection($pcode as xs:string)
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
for $doc in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")), cts:element-value-query(xs:QName("meta:pcode"), $pcode))
return
<item>{$doc/record/metadata/meta:isbns/meta:isbn13/fn:string()}</item>
}</Results>
};
declare function admin:getIsbnsByCollectionAndUploadDate($pcode as xs:string, $uploadDate as xs:string)
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
for $doc in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),cts:and-query(
(cts:element-value-query(xs:QName("meta:first-online-date"), $uploadDate),
cts:element-value-query(xs:QName("meta:pcode"), $pcode))))
return
<item> { $doc/record/metadata/meta:isbns/meta:isbn13/fn:string() } </item>
} </Results>
};
declare function admin:getIsbnDateByCollection($pcode as xs:string)
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
for $doc in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")), cts:element-value-query(xs:QName("meta:pcode"), $pcode))
return
<item>{
<isbn>{$doc/record/metadata/meta:isbns/meta:isbn13/fn:string()}</isbn>,
<date>{$doc/record/metadata/meta:pdates/meta:first-online-date/fn:string()}</date>
}
</item>
}</Results>
};
declare function admin:getDOIsByCollection($pcode as xs:string)
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
for $doc in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")), cts:element-value-query(xs:QName("pc:CollectionAcronym"), $pcode))
return
<item>{$doc/record/metadata/meta:doi/fn:string()}</item>
}</Results>
};
declare function admin:getDOIsByCollectionAndUploadDate($pcode as xs:string, $uploadDate as xs:string)
{
<Results> {
let $time-limit := xdmp:set-request-time-limit(60)
for $doc in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),cts:and-query(
(cts:element-value-query(xs:QName("meta:first-online-date"), $uploadDate),
cts:element-value-query(xs:QName("pc:CollectionAcronym"), $pcode))))
return
<item> { $doc/record/metadata/meta:doi/fn:string() } </item>
} </Results>
};
declare function admin:getXrefXMLforDOI($doi as xs:string)
{
let $time-limit := xdmp:set-request-time-limit(60)
let $doc := cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot", "palgraveconnect_chapter")), cts:element-value-query(xs:QName("meta:doi"), $doi))
let $prod := $doc/record/content/pc:ebook/pc:Product
let $cc := $doc/record/content/pc:ebook/pc:CustomConnect
return
<body>
<book book_type = "{ if ( $cc/pc:AuthorEdited/fn:string() eq 'Authored' )
then "monograph"
else "edited_book"}">
<book_metadata language="en">
<contributors> {
for $contr in $prod/pc:Contributor
return
<person_name sequence="first" contributor_role =
"{ if ( $contr/pc:ContributorRole/fn:string() eq 'A01')
then "author"
else "editor" }">
<given_name> { $contr/pc:NamesBeforeKey/fn:string() } </given_name>
<surname> { $contr/pc:PrefixToKey/fn:string(),
$contr/pc:KeyNames/fn:string(),
$contr/pc:NamesAfterKey } </surname>
<suffix> { $contr/pc:SuffixToKey } </suffix>
<organization sequence="first"
contributor_role="{ if ( $contr/pc:ContributorRole/fn:string() eq 'A01' )
then "author"
else "editor" }"> { $contr/pc:CorporateName/fn:string() } </organization>
</person_name>
} </contributors>
<titles>
<title> { $prod/pc:Title/pc:TitleText/fn:string() } </title>
</titles>
<edition_number> { $prod/pc:EditionNumber/fn:string() } </edition_number>
<publication_date media_type="print">
<month> {$doc/record/metadata/meta:pdates/meta:pdate-actual/@month/fn:string()}</month>
<day> {$doc/record/metadata/meta:pdates/meta:pdate-actual/@day/fn:string()}</day>
<year> {$doc/record/metadata/meta:pdates/meta:pdate-actual/@year/fn:string()}</year>
</publication_date>
<isbn> { $prod/pc:ProductIdentifier/pc:ISBNValue/fn:string() } </isbn>
<publisher>
<publisher_name> { $prod/pc:Publisher/pc:PublisherName/fn:string() } </publisher_name>
<publisher_place> { $prod/pc:CityOfPublication/fn:string() } </publisher_place>
</publisher>
</book_metadata>
{
admin:getXrefChapterDetails($prod)
}
</book>
</body>
};
declare function admin:getXrefChapterDetails($prod as node())
{
for $contentItem in $prod/pc:ContentItem[pc:TextItem/pc:TextItemType eq "03"][pc:MediaFile/pc:MediaFileTypeCode eq '01']
let $doi:=$contentItem/pc:TextItem/pc:TextItemIdentifier[pc:TextItemIDType eq '06'][1]/pc:IDValue/fn:string()
return
<content_item component_type="chapter">
{
<contributors>
{
for $contributor in $contentItem/pc:Contributor
return
<person_name sequence="first" contributor_role =
"{ if ( $contributor/pc:ContributorRole/fn:string() eq 'A01')
then "author"
else "editor" }">
<given_name> { $contributor/pc:NamesBeforeKey/fn:string() } </given_name>
<surname> { $contributor/pc:PrefixToKey/fn:string(),
$contributor/pc:KeyNames/fn:string(),
$contributor/pc:NamesAfterKey } </surname>
</person_name >
}
</contributors>
}
<titles>
<title>{$contentItem/pc:Title/pc:TitleText/fn:string()}</title>
</titles>
<doi_data>
<doi>{$doi}</doi>
<resource>http://www.palgraveconnect.com/doifinder/{$doi}</resource>
</doi_data>
</content_item>
};
declare function admin:getBooksCountByProductCodes($productCodes as xs:string)
{
<Results>
{
let $time-limit := xdmp:set-request-time-limit(60)
let $tokenized-productCodes := fn:tokenize($productCodes, ",")
for $pcode in $tokenized-productCodes
return
<count pcode="{$pcode}">{ xdmp:estimate(cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")), cts:element-value-query(xs:QName("meta:pcode"), $pcode))) }</count>
}
</Results>
};
declare function admin:updateUploadStatusByIsbns($isbnList as xs:string, $status as xs:string)
{
let $isbns := fn:tokenize($isbnList, ",")
for $isbn in $isbns
return
xdmp:eval('
declare namespace meta = "http://nature.com/meta";
declare namespace pc = "http://www.palgraveconnect.com/ebook";
declare variable $isbn as xs:string external;
declare variable $status as xs:string external;
let $doc := cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),cts:element-value-query(xs:QName("meta:isbn13"), $isbn))
return
if (fn:not($doc//pc:CustomConnect/pc:UploadStatus/text()))
then
xdmp:node-insert-child($doc//pc:CustomConnect,<pc:UploadStatus>text{$status}</pc:UploadStatus>)
else
xdmp:node-replace($doc//pc:CustomConnect/pc:UploadStatus/text() , text{$status})',
(xs:QName("isbn"), $isbn,xs:QName("status"), $status),
<options xmlns="xdmp:eval">
<isolation>different-transaction</isolation>
<prevent-deadlocks>true</prevent-deadlocks>
</options>)
};
declare function admin:getMarkLogicCollectionName($uri){
fn:string-join(xdmp:document-get-collections($uri), ',')
};
declare function admin:deleteDocuments($uriList as xs:string) {
let $documents-uri := fn:tokenize($uriList, ",")
for $document-uri in $documents-uri
let $result := xdmp:document-delete($document-uri)
return fn:concat($document-uri," , ")
};
declare function admin:getTitlesDetailsForMpsFeeds()
{
<results>{
let $time-limit := xdmp:set-request-time-limit(600)
let $documents := fn:collection(("PalgraveConnect", "palgrave_connect_pivot"))
for $document in $documents
order by $document/record/content/pc:ebook/pc:CustomConnect/pc:Collection/pc:CollectionISBN ascending
return
<item>{
<thirteen-digit-isbn>{$document/record/metadata/meta:isbns/meta:isbn13/fn:string()}</thirteen-digit-isbn>,
<doi>{$document/record/metadata/meta:doi/fn:string()}</doi>,
<title>{$document/record/metadata/meta:atitle/fn:string()}</title>,
<collection>
<collection-acronym>{$document/record/content/pc:ebook/pc:CustomConnect/pc:Collection/pc:CollectionAcronym/fn:string()}</collection-acronym>
<collection-isbn>{$document/record/content/pc:ebook/pc:CustomConnect/pc:Collection/pc:CollectionISBN/fn:string()}</collection-isbn>
<collection-workid>{$document/record/content/pc:ebook/pc:CustomConnect/pc:Collection/pc:CollectionWorkId/fn:string()}</collection-workid>
</collection>
}</item>
}</results>
};

declare function admin:getContentItemsForDOI($doi as xs:string)
{
<Results> {
for $book in cts:search(fn:collection(("PalgraveConnect", "palgrave_connect_pivot")),cts:element-value-query(xs:QName("meta:doi"), $doi))
return
$book/record/content/pc:ebook/pc:Product/pc:ContentItem
}</Results>
};