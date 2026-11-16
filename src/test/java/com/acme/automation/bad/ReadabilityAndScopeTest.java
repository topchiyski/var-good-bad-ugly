package com.acme.automation.bad;

import com.acme.automation.support.TestMocks.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SequencedCollection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * With {@code var}, names must carry the type information, or code becomes unreadable
 * outside an IDE, such as in a pull request diff.
 */
class ReadabilityAndScopeTest {

    // Opaque helper names (anti-pattern)

    /**
     * Opaque: the name hides that it returns a {@link By}.
     */
    private By get(String id) {
        return By.id(id);
    }

    /**
     * Opaque: the name hides that it returns a {@link WebElement}.
     */
    private WebElement find(By locator) {
        return new MockWebElement("button", "Submit");
    }

    /**
     * Opaque: the name hides what it processes and returns.
     */
    private OrderBatchResult processData(String id) {
        return new OrderBatchResult(200, id);
    }

    // Descriptive helper names (senior pattern)

    /**
     * Builds a locator for a test id.
     */
    private By buildTestIdLocator(String id) {
        return By.id(id);
    }

    /**
     * Waits until the located element is visible.
     */
    private WebElement waitForVisibleElement(By locator) {
        return new MockWebElement("button", "Submit");
    }

    /**
     * Returns {@code "ORD-1"} and {@code "ORD-2"}, in that order.
     */
    private List<String> fetchIds() {
        return List.of("ORD-1", "ORD-2");
    }

    /**
     * Bad: short variable names plus opaque helpers leave every type unknown.
     */
    @Test
    void checkoutFlow_Bad_OpaqueVarInBrowserDiff() {
        // BAD: In a GitHub PR diff without IDE tooltips, what types are 'r', 'el', and 'res'?
        var r = get("checkout-btn");
        var el = find(r);
        var res = processData(el.getAttribute("data-id"));

        assertThat(res.statusCode()).isEqualTo(200);
    }

    /**
     * Good: type information comes from the variable name, the method name, or an explicit type.
     */
    @Test
    void checkoutFlow_Good_ThreeChannelConservationLaw() {
        // GOOD: Descriptive variable & method names carry the semantic weight,
        // or explicit types are preserved when calling an opaque method like processData().
        var checkoutBtnLocator = buildTestIdLocator("checkout-btn");
        var checkoutSubmitBtn = waitForVisibleElement(checkoutBtnLocator);
        OrderBatchResult batchResult = processData(checkoutSubmitBtn.getAttribute("data-id"));

        assertThat(batchResult.statusCode()).isEqualTo(200);
    }

    /**
     * Bad: a {@code var} declared far from its use hides, at the point of use, that the
     * collection is a {@link HashSet}, which has no defined order.
     */
    @Test
    void scopeLeakage_JuniorTrap_DeclaredFarFromUse() {
        // JUNIOR TRAP: declared at the top of the test...
        var auditIds = new HashSet<>(fetchIds());

        var checkoutBtnLocator = buildTestIdLocator("checkout-btn");
        var checkoutSubmitBtn = waitForVisibleElement(checkoutBtnLocator);
        OrderBatchResult batchResult = processData(checkoutSubmitBtn.getAttribute("data-id"));
        assertThat(batchResult.statusCode()).isEqualTo(200);

        // ...and used here, where nothing on screen says "HashSet". The reader expects
        // FINAL_AUDIT_ORDER to be the last audit id, but a HashSet has no "last":
        // auditIds.getLast() would not even compile (CompileTimeTrapTest proves it).
        auditIds.add("FINAL_AUDIT_ORDER");

        assertThat(auditIds).isNotInstanceOf(SequencedCollection.class);
    }

    /**
     * Good: declare the {@code var} next to its use, with a type that keeps insertion order.
     */
    @Test
    void scopeLeakage_SeniorPattern_TightScopeOrderedType() {
        var orderedAuditIds = new ArrayList<>(fetchIds());
        orderedAuditIds.add("FINAL_AUDIT_ORDER");

        assertThat(orderedAuditIds.getLast()).isEqualTo("FINAL_AUDIT_ORDER");
        assertThat(orderedAuditIds).containsExactly("ORD-1", "ORD-2", "FINAL_AUDIT_ORDER");
    }
}
