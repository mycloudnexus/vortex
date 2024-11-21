package com.consoleconnect.vortex.iam.util;

import com.auth0.json.mgmt.Page;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class Auth0PageHelper {
  private Auth0PageHelper() {}

  private static final Integer MAX_PER_PAGE_SIZE = 100;

  public static <T> Paging<T> listPageByTotal(
      Integer size, Integer page, Function<PageFilterParameters, Page<T>> function) {
    log.info("listPageByTotal size:{}, page:{}", size, page);

    PageFilterParameters pageFilterParameters = new PageFilterParameters();
    pageFilterParameters.setPage(page);

    if (size == PagingHelper.ALL) {

      List<T> result = new ArrayList<>();
      size = MAX_PER_PAGE_SIZE;
      pageFilterParameters.setSize(size);
      Integer total = -1;
      Integer totalPages = 0;
      do {

        Page<T> entityPage = function.apply(pageFilterParameters);
        result.addAll(entityPage.getItems());
        if (total == -1) {
          total = entityPage.getTotal();
          totalPages = (total % size) == 0 ? total / size : (total / size + 1);
        }
        pageFilterParameters.setPage(++page);
      } while (page < totalPages);
      log.info("listPageByTotal-all total:{}", total);
      return PagingHelper.toPage(result, 0, total, total.longValue());

    } else {

      pageFilterParameters.setSize(size);
      Page<T> entityPage = function.apply(pageFilterParameters);
      List<T> result =
          CollectionUtils.isEmpty(entityPage.getItems())
              ? Collections.emptyList()
              : entityPage.getItems();

      return PagingHelper.toPageNoSubList(
          result, page, result.size(), entityPage.getTotal().longValue());
    }
  }

  public static <T> Paging<T> listPageByLimit(
      Integer size, Integer page, Function<PageFilterParameters, Page<T>> function) {
    log.info("listPageByLimit size:{}, page:{}", size, page);

    PageFilterParameters pageFilterParameters = new PageFilterParameters();
    pageFilterParameters.setPage(page);

    if (size == PagingHelper.ALL) {

      List<T> result = new ArrayList<>();
      size = MAX_PER_PAGE_SIZE;
      pageFilterParameters.setSize(size);
      Integer total = 0;
      int limitOfResponse = 0;

      do {
        Page<T> entityPage = function.apply(pageFilterParameters);
        result.addAll(entityPage.getItems());
        limitOfResponse = entityPage.getLimit();
        total += limitOfResponse;
        pageFilterParameters.setPage(++page);

        // If the current limit is not equal with the passed size, it means no more records.
      } while (limitOfResponse == size);

      log.info("listPageByLimit-all total:{}", total);
      return PagingHelper.toPage(result, 0, total, total.longValue());

    } else {

      pageFilterParameters.setSize(size);
      Page<T> entityPage = function.apply(pageFilterParameters);
      List<T> result =
          CollectionUtils.isEmpty(entityPage.getItems())
              ? Collections.emptyList()
              : entityPage.getItems();

      return PagingHelper.toPageNoSubList(result, page, entityPage.getLimit(), null);
    }
  }

  @Data
  public static class PageFilterParameters {
    private boolean includeTotals = true;
    private int page;
    private int size;
  }
}
