package com.duan.blogos.web.api.audience;

import com.duan.blogos.common.Order;
import com.duan.blogos.common.Rule;
import com.duan.blogos.dto.blog.BlogCommentDTO;
import com.duan.blogos.dto.blog.BlogListItemDTO;
import com.duan.blogos.dto.blog.BlogMainContentDTO;
import com.duan.blogos.entity.blogger.BloggerAccount;
import com.duan.blogos.enums.BlogStatusEnum;
import com.duan.blogos.exception.*;
import com.duan.blogos.manager.AudiencePropertiesManager;
import com.duan.blogos.manager.BlogSortRule;
import com.duan.blogos.result.ResultBean;
import com.duan.blogos.service.audience.BlogBrowseService;
import com.duan.blogos.service.audience.BlogRetrievalService;
import com.duan.blogos.service.blogger.BloggerAccountService;
import com.duan.blogos.util.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created on 2017/12/19.
 *
 * @author DuanJiaNing
 */
@ControllerAdvice
@RequestMapping("/blog/get")
public class BlogController {

    @Autowired
    private AudiencePropertiesManager audiencePropertiesManager;

    @Autowired
    private BlogRetrievalService retrievalService;

    @Autowired
    private BloggerAccountService bloggerAccountService;

    @Autowired
    private BlogBrowseService blogBrowseService;

    /*
     * 检查博文是否存在
     */
    private void checkBlog(Integer blogId, RequestContext context) {
        if (blogId == null) {
            throw new UnknownBlogException(context.getMessage("blog.unknownBlog"));
        }
    }

    /*
     * 检查博主是否存在
     */
    private BloggerAccount checkAccount(Integer id, RequestContext context) {
        BloggerAccount account;
        if (id == null || (account = bloggerAccountService.getAccount(id)) == null) {
            throw new UnknownBloggerException(context.getMessage("blog.unknownBlogger"));
        }
        return account;
    }

    /*
     * 统一处理异常
     */
    @ExceptionHandler(BaseRuntimeException.class)
    @ResponseBody
    public ResultBean exceptionHandler(Throwable e) {
        return new ResultBean(e);
    }

    /*
     * 处理结果为空的情况
     */
    private void handlerEmptyResult(RequestContext context) {
        throw new EmptyResultException(context.getMessage("blog.emptyResult"));
    }

    /**
     * 检索博文
     * 文档见 doc/wiki/博主博文检索.md
     * 查询时博文状态调用者无法指定，只能查询 {@link BlogStatusEnum#PUBLIC}的
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<List<BlogListItemDTO>> bloggerBlogList(HttpServletRequest request,
                                                             @RequestParam("bloggerId") Integer bloggerId,
                                                             @RequestParam(value = "cids", required = false) String categoryIds,
                                                             @RequestParam(value = "lids", required = false) String labelIds,
                                                             @RequestParam(value = "kword", required = false) String keyWord,
                                                             @RequestParam(value = "offset", required = false) Integer offset,
                                                             @RequestParam(value = "rows", required = false) Integer rows,
                                                             @RequestParam(value = "sort", required = false) String sort,
                                                             @RequestParam(value = "order", required = false) String order) {
        final RequestContext context = new RequestContext(request);

        //检查账户
        BloggerAccount account = checkAccount(bloggerId, context);

        //检查数据合法性
        String sor = sort == null ? Rule.VIEW_COUNT.name() : sort.toUpperCase();
        String ord = order == null ? Order.DESC.name() : order.toUpperCase();
        String ch = audiencePropertiesManager.getUrlConditionSplitCharacter();
        checkProperties(categoryIds, ch, labelIds, ch, sor, ord, context);

        //执行数据查询
        BlogSortRule rule = new BlogSortRule(Rule.valueOf(sor), Order.valueOf(ord));

        int[] cids = StringUtils.intStringDistinctToArray(categoryIds, ch);
        int[] lids = StringUtils.intStringDistinctToArray(labelIds, ch);

        int os = offset == null || offset < 0 ? 0 : offset;
        int rs = rows == null || rows < 0 ? audiencePropertiesManager.getRequestBloggerBlogListCount() : rows;
        ResultBean<List<BlogListItemDTO>> listResultBean = retrievalService.listFilterAll(cids, lids, keyWord, account.getId(), os, rs, rule);

        if (listResultBean == null) handlerEmptyResult(context);

        return listResultBean;
    }

    /*
     * 检查输入
     */
    private void checkProperties(String categoryIds, String cr, String labelIds, String lr,
                                 String sort, String order, RequestContext context) {

        if (categoryIds != null && !StringUtils.isIntStringSplitByChar(categoryIds, cr)) {
            throw new StringSplitException(context.getMessage("blog.stringSplitIllegal"));
        }

        if (labelIds != null && !StringUtils.isIntStringSplitByChar(labelIds, lr)) {
            throw new StringSplitException(context.getMessage("blog.stringSplitIllegal"));
        }

        if (sort != null && !Rule.contains(sort)) {
            throw new BlogSortRuleUndefinedException(context.getMessage("blog.blogSortRuleUndefined"));
        }

        if (order != null && !Order.contains(order)) {
            throw new BlogSortOrderUndefinedException(context.getMessage("blog.blogSortOrderUndefined"));
        }
    }

    /**
     * 获得博文主体内容
     * 文档见 doc/wiki/博文主体内容.md
     */
    @RequestMapping(value = "/content", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<BlogMainContentDTO> bloggerBlogMainContent(HttpServletRequest request,
                                                                 @Param("blogId") Integer blogId) {
        final RequestContext context = new RequestContext(request);

        //检查博文是否存在
        checkBlog(blogId, context);
        ResultBean<BlogMainContentDTO> mainContent = blogBrowseService.getBlogMainContent(blogId);
        if (mainContent == null) handlerEmptyResult(context);

        return mainContent;
    }

    /**
     * 获得博文评论列表
     * 文档见 doc/wiki/博文评论列表.md
     */
    @RequestMapping("/comment")
    @ResponseBody
    public ResultBean<List<BlogCommentDTO>> bloggerBlogComment(HttpServletRequest request,
                                                               @Param("blogId") Integer blogId,
                                                               @RequestParam(value = "offset", required = false) Integer offset,
                                                               @RequestParam(value = "rows", required = false) Integer rows) {
        final RequestContext context = new RequestContext(request);

        //检查博文是否存在
        checkBlog(blogId, context);
        int os = offset == null || offset < 0 ? 0 : offset;
        int rs = rows == null || rows < 0 ? 0 : audiencePropertiesManager.getRequestBloggerBlogCommentCount();
        ResultBean<List<BlogCommentDTO>> resultBean = blogBrowseService.listBlogComment(blogId, os, rs);
        if (resultBean == null) handlerEmptyResult(context);

        return resultBean;
    }

}