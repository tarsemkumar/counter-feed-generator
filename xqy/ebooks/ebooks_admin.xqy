xquery version '1.0-ml';

declare function admin:getTitlesDetailsForMpsFeeds()
{
    <results>{
    let $time-limit := xdmp:set-request-time-limit(600)
    let $documents := fn:collection(("PalgraveConnect", "palgrave_connect_pivot"))
            
    for $document in $documents
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