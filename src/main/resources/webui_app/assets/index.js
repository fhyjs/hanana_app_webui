addNavButton("../webui_app/pages/app/pages.html","页面");
$('#content-iframe').on('load', function() {
    var iframe = $('#content-iframe')[0];
    var ifdoc = $(iframe.contentWindow.document);
    if (iframe.contentWindow.location.href.includes("pages/settings.html")){
        var data = $.parseJSON(synchronousPostRequest(`../../../../../../data/settings/wa/config_list.json`,""));
        data.forEach(function (item) {
            item.classes.forEach(function (clazz) {
                ifdoc.find("#links").append(`<li><a href='../../webui_app/pages/app/settings/common.html?id=${item.id}&class=${clazz['class']}'>${item.name}/${clazz.name}</a></li>`);
            });
        });
    }
});
