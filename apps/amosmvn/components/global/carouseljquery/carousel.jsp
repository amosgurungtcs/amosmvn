<%@include file="/libs/foundation/global.jsp"%>
<cq:includeClientLib categories="carousel" />

<script type="text/javascript">
$(function(){
    $('#carousel').infiniteCarousel({
        displayTime: 6000,
        textholderHeight : .25
    });
});
</script>
<style type="text/css">
body {
    padding-top: 50px;
}
#carousel {
    margin: 0 auto;
    width: 400px;
    height: 390px;
    padding: 0;
    overflow: scroll;
    border: 2px solid #999;
}
#carousel ul {
    list-style: none;
    width: 1500px;
    margin: 0;
    padding: 0;
    position: relative;
}
#carousel li {
    display: inline;
    float: left;
}
.textholder {
    text-align: left;
    font-size: small;
    padding: 6px;
    -moz-border-radius: 6px 6px 0 0;
    -webkit-border-top-left-radius: 6px;
    -webkit-border-top-right-radius: 6px;
}
</style>


<div id="carousel">
    <ul>
        <li><img alt="" src="/content/dam/Amos/Mobile/leeco-le-2-le-x526-original-imaemwfggqfnhpn9.jpeg" /><p>Image 1.</p>
        </li>
        <li><img alt="" src="/content/dam/Amos/Mobile/lenovo-k5-note-pa330118in-original-imaekyaqxwzfhacu.jpeg" /><p>Image 2</p>
        </li>
        <li><img alt="" src="/content/dam/Amos/Mobile/lenovo-k5-plus-3gb-a6020a46-original-imaekefcwbt7qdue.jpeg" width="500" height="213" /><p>Image 3</p>
        </li>
        <li><img alt="" src="/content/dam/Amos/Mobile/lenovo-vibe-k5-plus-3-gb-a6020a46-original-imaektyuztjsgzba.jpeg" width="500" height="213" /><p>Image 4</p>
        </li>
        <li><img alt="" src="/content/dam/Amos/Mobile/letv-le-1s-eco-na-original-imaegcqyaamyt9gx.jpeg" width="500" height="213" /><p>Image 5</p>
        </li>
    </ul>
</div>