import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { FilterBar } from "../../../src/components/feed/FilterBar";

const defaultFilters = {
  category: "",
  tags: [],
  dietary: [],
  allergens: [],
  priceRange: "",
  radius: 5
};

describe("FilterBar", () => {
  it("updates category and price filters", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(<FilterBar filters={defaultFilters} onChange={onChange} />);

    await user.selectOptions(screen.getByRole("combobox"), "Bakery");
    expect(onChange).toHaveBeenLastCalledWith({ ...defaultFilters, category: "Bakery" });

    await user.click(screen.getByRole("button", { name: "Free only" }));
    expect(onChange).toHaveBeenLastCalledWith({ ...defaultFilters, priceRange: "free" });
  });

  it("toggles multi-select filter chips", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(
      <FilterBar
        filters={{ ...defaultFilters, tags: ["Vegan"] }}
        onChange={onChange}
      />
    );

    await user.click(screen.getAllByRole("button", { name: "Vegan" })[0]);
    expect(onChange).toHaveBeenLastCalledWith({ ...defaultFilters, tags: [] });
  });

  it("shows active filters and clears everything back to defaults", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(
      <FilterBar
        filters={{
          category: "Prepared",
          tags: ["Halal"],
          dietary: [],
          allergens: ["Dairy"],
          priceRange: "under3",
          radius: 10
        }}
        onChange={onChange}
      />
    );

    expect(screen.getAllByText("Prepared")).toHaveLength(2);
    expect(screen.getByText("No Dairy")).toBeInTheDocument();
    expect(screen.getAllByText("10 mi")).toHaveLength(2);

    await user.click(screen.getByRole("button", { name: "Clear all" }));
    expect(onChange).toHaveBeenLastCalledWith(defaultFilters);
  });
});
