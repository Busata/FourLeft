ALTER TABLE merge_results_view
    ADD merge_mode VARCHAR(255);

update merge_results_view set merge_mode = 'ADD_TIMES';