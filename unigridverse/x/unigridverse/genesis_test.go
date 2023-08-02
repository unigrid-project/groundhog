package unigridverse_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	keepertest "unigridverse/testutil/keeper"
	"unigridverse/testutil/nullify"
	"unigridverse/x/unigridverse"
	"unigridverse/x/unigridverse/types"
)

func TestGenesis(t *testing.T) {
	genesisState := types.GenesisState{
		Params: types.DefaultParams(),

		// this line is used by starport scaffolding # genesis/test/state
	}

	k, ctx := keepertest.UnigridverseKeeper(t)
	unigridverse.InitGenesis(ctx, *k, genesisState)
	got := unigridverse.ExportGenesis(ctx, *k)
	require.NotNil(t, got)

	nullify.Fill(&genesisState)
	nullify.Fill(got)

	// this line is used by starport scaffolding # genesis/test/assert
}
