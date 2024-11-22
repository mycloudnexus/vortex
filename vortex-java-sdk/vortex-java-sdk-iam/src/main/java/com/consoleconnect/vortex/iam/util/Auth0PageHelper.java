package com.consoleconnect.vortex.iam.util;

import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.json.mgmt.Page;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Auth0PageHelper {
  private Auth0PageHelper() {}

  private static final Integer MAX_SIZE_PER_PAGE = 100;

  public static <T> Paging<T> loadData(
      int page, int size, Function<PageFilterParameters, Page<T>> function) {
    log.info("loadData page:{}, size:{}", page, size);

    int pageSize = Math.max(size, MAX_SIZE_PER_PAGE);
    int pageTotalSize = size == PagingHelper.ALL ? Integer.MAX_VALUE : pageSize;

    Integer total = 0;
    List<T> result = new ArrayList<>();

    do {
      log.info("load data: page:{}, pageSize:{}", page, pageSize);
      Page<T> entityPage =
          function.apply(
              PageFilterParameters.builder().page(page).size(pageSize).includeTotals(true).build());
      result.addAll(entityPage.getItems());
      total = entityPage.getTotal();
      log.info(
          "loaded data:currentSize:{}, loadedTotal:{},requiredTotal:{},",
          entityPage.getItems().size(),
          result.size(),
          pageTotalSize);
      if (result.size() >= pageTotalSize || entityPage.getItems().size() < pageSize) {
        break;
      }
      page++;
    } while (true);

    log.info("loadData done, loadedTotal:{}", result.size());
    return PagingHelper.toPage(
        result, page, result.size(), total != null ? total.longValue() : null);
  }

  @Data
  @SuperBuilder
  public static class PageFilterParameters {
    private boolean includeTotals;
    private int page;
    private int size;

    public PageFilter toPageFilter() {
      return new PageFilter().withPage(page, size).withTotals(includeTotals);
    }
  }
}
